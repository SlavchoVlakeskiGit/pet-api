package com.example.petapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PetApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetApiApplication.class, args);
    }
}
