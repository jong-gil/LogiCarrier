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

    public String setWorkerBit(int positionNum, String userType) {
        if (-1 < positionNum && positionNum < 5) {
            ValueOperations<String, String> redisBit = redisTemplate.opsForValue();
            String workerBit = redisBit.get("workerBit");
            StringBuilder redisBitStr = new StringBuilder(workerBit);
            if (userType.equals("picker")) {
                redisBitStr.setCharAt(positionNum, '1');
            } else if (userType.equals("pusher")) {
                redisBitStr.setCharAt(positionNum, '2');
            }
            redisBit.set("workerBit", redisBitStr.toString());
            return "Worker Position has Changed";
        }
        return "WRONG workerId";
    }
    public String setProgressBit(Long workerId) {
        if (-1 < workerId && workerId < 5) {
            ValueOperations<String, String> redisBit = redisTemplate.opsForValue();
            String progressBit = redisBit.get("progressBit");
            String changedProgressBit = getChangedString(workerId, progressBit);
            redisBit.set("progressBit", changedProgressBit);
            return "Worker's Progress status has Changed";
        }
        return "WRONG workerId";
    }

    private String getChangedString(Long workerId, String redisBit) {
        assert redisBit != null;

        StringBuilder redisBitStr = new StringBuilder(redisBit);
        if (redisBitStr.charAt(workerId.intValue()) == '0') {
            redisBitStr.setCharAt(workerId.intValue(), '1');
        }
        redisBitStr.setCharAt(workerId.intValue(), '0');
        return redisBitStr.toString();
    }

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

