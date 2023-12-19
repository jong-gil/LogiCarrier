package com.example.workerservice.service;

import com.example.workerservice.dto.OrderDto;
import com.example.workerservice.dto.WorkerReq;
import com.example.workerservice.dto.WorkerRes;
import com.example.workerservice.dto.ResponseItem;
import com.example.workerservice.entity.WorkerEntity;
import com.example.workerservice.messagequeue.KafkaProducer;
import com.example.workerservice.repository.WorkerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService{
    private final WorkerRepository workerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;
    private final KafkaProducer kafkaProducer;
    private final RedisService redisService;
    private final RestTemplate restTemplate;
    private final Environment env;

    @Override
    @Transactional
    public String pickItem(String workerId, long itemId) throws Exception {
        ZSetOperations<String, Object> redisSortedSet = redisTemplate.opsForZSet();
        StringBuilder sb = new StringBuilder("worker");

        int workerInt = Integer.parseInt(workerId) + 1;

        if (0 < workerInt && workerInt < 6) {
            String workerBitKey = String.valueOf(sb.append(workerInt));
            Object target = Objects.requireNonNull(redisSortedSet.popMin(workerBitKey)).getValue();
            Object nextTarget = Objects.requireNonNull(redisSortedSet.range(workerBitKey, 0, 0));

            return process(itemId, target, nextTarget, workerId);
        } else {
            return "Picker is NOT PRESENT";
        }
    }

    @Override
    @Transactional
    public String changePosition(String workerId) {
        Long pickerIdL = Long.parseLong(workerId);
        Optional<WorkerEntity> worker = workerRepository.findByWorkerId(pickerIdL);

        if (worker.isPresent()) {
            boolean currentStatus = worker.get().isStatus();
            worker.get().changeStatus(currentStatus);
            workerRepository.save(worker.get());

            boolean changedStatus = worker.get().isStatus();
            if (changedStatus) {
                return " worker is now Pusher";
            }
            return "worker is now Picker";
        }
        return "worker is NOT PRESENT";
    }

    private String process(long itemId, Object target, Object nextTarget, String workerId) throws Exception {
        WorkerRes targetPayload = mapper.convertValue(target, WorkerRes.class);
        WorkerRes nextTargetPayload = mapper.convertValue(nextTarget, WorkerRes.class);

        ArrayList<ResponseItem> itemList = targetPayload.getResponseItemList();

        // 지금 orderId와 다음 orderId가 같다면 같은 주문임
        if (targetPayload.getOrderId() == nextTargetPayload.getOrderId()) {
            // 주문에 해당하는 item처리
            itemList.removeIf(item -> item.getId() == itemId);
            // 하나의 route 처리 완료 -> robotService로 출발 신호 보내기
            WorkerReq pickedItem = WorkerReq.builder()
                    .robotId(targetPayload.getRobotId())
                    .shelfId(targetPayload.getShelfId())
                    .responseItemList(itemList)
                    .build();
            kafkaProducer.pickedItem("PickerToRobot", pickedItem);
            redisService.setProgressBit(Long.parseLong(workerId));

            return itemId + "is Picked";
        } // 다르다면 하나의 주문이 처리된 것 -> orderService로 complete 보냄
        else {
            // 주문에 해당하는 item처리
            itemList.removeIf(item -> item.getId() == itemId);
            // 하나의 route 처리 완료 -> robotService로 출발 신호 보내기
            WorkerReq pickedItem = WorkerReq.builder()
                    .robotId(targetPayload.getRobotId())
                    .shelfId(targetPayload.getShelfId())
                    .build();
            kafkaProducer.pickedItem("PickerToRobot", pickedItem);

            WorkerReq orderCompleted = WorkerReq.builder()
                    .workerId(workerId)
                    .orderStatus(true)
                    .build();
            completedOrder(orderCompleted);
            redisService.setProgressBit(Long.parseLong(workerId));
            return itemId + "is Picked"
                    + "%n order no." + targetPayload.getOrderId() + "is Completed";
        }
    }

    private void completedOrder(WorkerReq workerReq) {
        String orderUrl = String.format(env.getProperty("order_service.url"));
        ResponseEntity<OrderDto> responseData = restTemplate.postForEntity(orderUrl, workerReq, OrderDto.class);
    }
}
