package com.example.robotservice.Repoistory;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(String key) {
        return redisTemplate
                .opsForValue()
                //setnx 명령어 사용 - key(key) value("lock")
                .setIfAbsent(key, "lock", Duration.ofMillis(3_000));
    }

    public Boolean unlock(String key) {
        return redisTemplate.delete(key);
    }
    public String get(String key){
        String res = redisTemplate.opsForValue().get(key);
        return res;
    }
    public void set(String key, String value){
        redisTemplate.opsForValue().set(key, value);
    }
    public void delete(String key, String hashKey){
        redisTemplate.opsForHash().delete(hashKey);
    }
}