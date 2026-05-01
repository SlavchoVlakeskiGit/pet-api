package com.example.petapi.config;

import com.example.petapi.mapper.PetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public PetMapper petMapper() {
        return PetMapper.INSTANCE;
    }
}
