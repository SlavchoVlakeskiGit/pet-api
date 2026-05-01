package com.example.petapi;

import com.example.petapi.repository.JpaPetRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7"));

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JpaPetRepository petRepository;

    private String token;
    private String adminToken;

    @BeforeAll
    void setupAuth() {
        Map<String, String> credentials = Map.of("username", "testuser", "password", "secret123");
        restTemplate.postForEntity("/auth/register", credentials, Void.class);
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/auth/login", credentials, Map.class);
        token = (String) loginResponse.getBody().get("token");
        assertNotNull(token, "Login must return a JWT token");

        // admin user is seeded by DataInitializer on context startup
        ResponseEntity<Map> adminLogin = restTemplate.postForEntity(
                "/auth/login", Map.of("username", "admin", "password", "admin123"), Map.class);
        adminToken = (String) adminLogin.getBody().get("token");
        assertNotNull(adminToken, "Admin login must return a JWT token");
    }

    @BeforeEach
    void clearPets() {
        petRepository.deleteAllInBatch();
    }

    private HttpEntity<Object> withAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> withAdminAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void getPets_returnsEmptyPage() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/pets", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().get("totalElements"));
    }

    @Test
    void createPet_withoutAuth_returns403() {
        Map<String, Object> body = Map.of("name", "Rex", "species", "Dog");

        ResponseEntity<String> response = restTemplate.postForEntity("/pets", body, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createPet_withAuth_returns201WithPetData() {
        Map<String, Object> body = Map.of("name", "Rex", "species", "Dog", "age", 3);

        ResponseEntity<Map> response = restTemplate.exchange("/pets", HttpMethod.POST, withAuth(body), Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("id"));
        assertEquals("Rex", response.getBody().get("name"));
        assertEquals("Dog", response.getBody().get("species"));
        assertEquals(3, response.getBody().get("age"));
    }

    @Test
    void getPetById_returnsPet() {
        ResponseEntity<Map> created = restTemplate.exchange(
                "/pets", HttpMethod.POST, withAuth(Map.of("name", "Luna", "species", "Cat")), Map.class);
        Integer id = (Integer) created.getBody().get("id");

        ResponseEntity<Map> response = restTemplate.getForEntity("/pets/" + id, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Luna", response.getBody().get("name"));
        assertEquals("Cat", response.getBody().get("species"));
    }

    @Test
    void getPetById_nonExistent_returns404() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/pets/99999", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updatePet_updatesOnlyProvidedFields() {
        ResponseEntity<Map> created = restTemplate.exchange(
                "/pets", HttpMethod.POST, withAuth(Map.of("name", "Buddy", "species", "Dog")), Map.class);
        Integer id = (Integer) created.getBody().get("id");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/pets/" + id, HttpMethod.PATCH, withAuth(Map.of("name", "Buddy Jr", "age", 2)), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Buddy Jr", response.getBody().get("name"));
        assertEquals("Dog", response.getBody().get("species")); // unchanged
        assertEquals(2, response.getBody().get("age"));
    }

    @Test
    void deletePet_softDeletes_andReturns404OnGet() {
        ResponseEntity<Map> created = restTemplate.exchange(
                "/pets", HttpMethod.POST, withAuth(Map.of("name", "Milo", "species", "Rabbit")), Map.class);
        Integer id = (Integer) created.getBody().get("id");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/pets/" + id, HttpMethod.DELETE, withAdminAuth(null), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<Map> getResponse = restTemplate.getForEntity("/pets/" + id, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void createPet_missingRequiredFields_returns400() {
        Map<String, Object> body = Map.of("age", 3); // name and species missing

        ResponseEntity<Map> response = restTemplate.exchange("/pets", HttpMethod.POST, withAuth(body), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getPets_withSpeciesFilter_returnsOnlyMatchingPets() {
        restTemplate.exchange("/pets", HttpMethod.POST, withAuth(Map.of("name", "Rex", "species", "Dog")), Map.class);
        restTemplate.exchange("/pets", HttpMethod.POST, withAuth(Map.of("name", "Luna", "species", "Cat")), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity("/pets?species=Dog", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get("totalElements"));
    }

    @Test
    void createPet_withIdempotencyKey_returnsSameResponseOnDuplicate() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Idempotency-Key", "test-key-" + System.nanoTime());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(Map.of("name", "Dupe", "species", "Dog"), headers);

        ResponseEntity<Map> first = restTemplate.exchange("/pets", HttpMethod.POST, request, Map.class);
        ResponseEntity<Map> second = restTemplate.exchange("/pets", HttpMethod.POST, request, Map.class);

        assertEquals(HttpStatus.CREATED, first.getStatusCode());
        assertEquals(HttpStatus.CREATED, second.getStatusCode());
        assertEquals(first.getBody().get("id"), second.getBody().get("id"));
    }

    @Test
    void getAuditLog_afterCreate_returnsEntry() {
        ResponseEntity<Map> created = restTemplate.exchange(
                "/pets", HttpMethod.POST, withAuth(Map.of("name", "AuditPet", "species", "Cat")), Map.class);
        Integer id = (Integer) created.getBody().get("id");

        ResponseEntity<Object[]> audit = restTemplate.exchange(
                "/pets/" + id + "/audit", HttpMethod.GET, withAdminAuth(null), Object[].class);

        assertEquals(HttpStatus.OK, audit.getStatusCode());
        assertTrue(audit.getBody().length >= 1);
    }
}
