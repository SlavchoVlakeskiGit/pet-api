package com.example.petapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@OpenAPIDefinition(info = @Info(
        title = "Pet API",
        version = "1.0",
        description = "REST API for managing pets"
))
public class PetApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetApiApplication.class, args);
    }
}
