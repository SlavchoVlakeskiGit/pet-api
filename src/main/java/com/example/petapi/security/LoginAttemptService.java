package com.example.petapi.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    private static final String ATTEMPTS_PREFIX = "login_attempts:";
    private static final String LOCKED_PREFIX = "login_locked:";

    private final StringRedisTemplate redisTemplate;

    public LoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isLocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCKED_PREFIX + username));
    }

    public void loginSucceeded(String username) {
        redisTemplate.delete(ATTEMPTS_PREFIX + username);
        redisTemplate.delete(LOCKED_PREFIX + username);
    }

    public void loginFailed(String username) {
        String key = ATTEMPTS_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(LOCKED_PREFIX + username, "locked",
                    LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(key);
        }
    }
}
