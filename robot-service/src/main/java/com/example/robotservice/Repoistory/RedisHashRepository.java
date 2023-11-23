package com.example.robotservice.Repoistory;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisHashRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
    public Boolean lock(String key) {
        return redisTemplate
                .opsForHash()
                //setnx 명령어 사용 - key(key) value("lock")
                .putIfAbsent(key, "lock", Duration.ofMillis(3_000));
    }

    public Boolean unlock(String key) {
        return redisTemplate.delete(key);
    }

    public String get(String key, String hashKey){
        String res = hashOperations.get(key, hashKey);
        return res;
    }
    public Boolean hasKey(String key, String hashKey){
        return hashOperations.hasKey(key, hashKey);
    }
    public void put(String key, String hashKey, String value){
        hashOperations.put(key, hashKey, value);
    }

}