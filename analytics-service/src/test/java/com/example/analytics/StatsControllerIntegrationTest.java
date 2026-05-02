package com.example.analytics;

import com.example.analytics.repository.DailyStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"pet-events"})
class StatsControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DailyStatsRepository dailyStatsRepository;

    @Value("${analytics.api-key}")
    private String apiKey;

    @BeforeEach
    void clearStats() {
        dailyStatsRepository.deleteAllInBatch();
    }

    private HttpEntity<Void> withApiKey() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        return new HttpEntity<>(headers);
    }

    @Test
    void getOverallStats_withApiKey_returns200() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/stats", HttpMethod.GET, withApiKey(), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("totalCreated"));
    }

    @Test
    void getOverallStats_withoutApiKey_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v1/stats", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getDailyStats_withApiKey_returns200() {
        ResponseEntity<Object[]> response = restTemplate.exchange(
                "/v1/stats/daily?days=7", HttpMethod.GET, withApiKey(), Object[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getDailyStats_invalidDays_returns400() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/stats/daily?days=0", HttpMethod.GET, withApiKey(), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getDailyStats_tooManyDays_returns400() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/stats/daily?days=91", HttpMethod.GET, withApiKey(), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void healthEndpoint_isPublic() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
