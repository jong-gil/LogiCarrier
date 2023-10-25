package com.example.workerservice.service;

import com.example.workerservice.entity.WorkerEntity;
import com.example.workerservice.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final WorkerRepository workerRepository;

    public void setInitialWorkerBit() {
        ValueOperations<String, String> redisBit = redisTemplate.opsForValue();
        List<WorkerEntity> workers = workerRepository.findAll();
        StringBuilder workerStr = new StringBuilder();
        for (WorkerEntity worker : workers) {
            if (worker.isStatus()) {
                workerStr.append("1");
            } workerStr.append("0");
        }
        redisBit.set("workerBit", String.valueOf(workerStr));
    }

    /*public void setWorkerBit(Long workerId) {
        ValueOperations<String, String> redisBit = redisTemplate.opsForValue();
        StringBuffer workerStr = new StringBuffer();

    }*/

    public void setInitialProgressBit() {
        ValueOperations<String, String> redisBit = redisTemplate.opsForValue();
        List<WorkerEntity> workers = workerRepository.findAll();
        StringBuilder progressStr = new StringBuilder();
        for (WorkerEntity worker : workers) {
            if (worker.isProgress()) {
                progressStr.append("1");
            } progressStr.append("0");
        }
        redisBit.set("workerBit", String.valueOf(progressStr));
    }

    @Transactional(readOnly = true)
    public String getBit(String key) {
        ValueOperations<String, String> redisBit = redisTemplate.opsForValue();
        return redisBit.get(key);
    }
}
