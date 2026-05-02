package com.example.petapi.security;

import com.example.petapi.model.RefreshToken;
import com.example.petapi.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RefreshToken create(String username) {
        repository.deleteByUsername(username);
        RefreshToken token = new RefreshToken(
                UUID.randomUUID().toString(),
                username,
                LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        );
        return repository.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            repository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expired");
        }
        return refreshToken;
    }

    @Transactional
    public void deleteByUsername(String username) {
        repository.deleteByUsername(username);
    }
}
