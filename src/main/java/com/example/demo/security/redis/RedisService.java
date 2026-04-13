package com.example.demo.security.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    private String key(Integer userId) {
        return "refresh:user:" + userId;
    }

    public void save(Integer userId, String refreshToken, long expireMin) {
        redisTemplate.opsForValue().set(
                key(userId),
                refreshToken,
                expireMin,
                TimeUnit.MINUTES
        );
    }

    public String get(Integer userId) {
        return (String) redisTemplate.opsForValue().get(key(userId));
    }

    public void delete(Integer userId) {
        redisTemplate.delete(key(userId));
    }

    public boolean validate(Integer userId, String refreshToken) {
        String saved = get(userId);
        return saved != null && saved.equals(refreshToken);
    }
}
