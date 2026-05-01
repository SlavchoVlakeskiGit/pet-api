package com.example.petapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@Order(1)
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String HEADER = "Idempotency-Key";
    private static final String KEY_PREFIX = "idempotency:";

    private final StringRedisTemplate redisTemplate;

    public IdempotencyFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idempotencyKey = request.getHeader(HEADER);

        if (idempotencyKey == null || !HttpMethod.POST.matches(request.getMethod())
                || !request.getRequestURI().startsWith("/v1/pets")) {
            filterChain.doFilter(request, response);
            return;
        }

        String redisKey = KEY_PREFIX + idempotencyKey;
        String cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            log.debug("Idempotency hit for key={}", idempotencyKey);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(cached);
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        int status = wrapper.getStatus();
        if (status == HttpServletResponse.SC_CREATED || status == HttpServletResponse.SC_OK) {
            String body = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (!body.isEmpty()) {
                redisTemplate.opsForValue().set(redisKey, body, Duration.ofHours(24));
                log.debug("Idempotency key={} stored for 24h", idempotencyKey);
            }
        }

        wrapper.copyBodyToResponse();
    }
}
