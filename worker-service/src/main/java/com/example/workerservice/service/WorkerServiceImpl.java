package com.example.workerservice.service;

import com.example.workerservice.dto.PickerReq;
import com.example.workerservice.dto.PickerRes;
import com.example.workerservice.dto.ResponseItem;
import com.example.workerservice.entity.WorkerEntity;
import com.example.workerservice.messagequeue.KafkaProducer;
import com.example.workerservice.repository.WorkerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService{
    private final WorkerRepository workerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public String pickItem(String pickerId, long itemId) {
        ZSetOperations<String, Object> redisSortedSet = redisTemplate.opsForZSet();
        String result = "";

        switch (pickerId) {
            case "1": {
                Object target = Objects.requireNonNull(redisSortedSet.popMin("worker1")).getValue();
                Object nextTarget = Objects.requireNonNull(redisSortedSet.range("worker1", 0, 0));

                return process(itemId, target, nextTarget);
            }
            case "2": {
                Object target = Objects.requireNonNull(redisSortedSet.popMin("worker2")).getValue();
                Object nextTarget = Objects.requireNonNull(redisSortedSet.range("worker2", 0, 0));

                return process(itemId, target, nextTarget);
            }
            case "3": {
                Object target = Objects.requireNonNull(redisSortedSet.popMin("worker3")).getValue();
                Object nextTarget = Objects.requireNonNull(redisSortedSet.range("worker3", 0, 0));

                return process(itemId, target, nextTarget);
            }
            case "4": {
                Object target = Objects.requireNonNull(redisSortedSet.popMin("worker4")).getValue();
                Object nextTarget = Objects.requireNonNull(redisSortedSet.range("worker4", 0, 0));

                return process(itemId, target, nextTarget);
            }
            case "5": {
                Object target = Objects.requireNonNull(redisSortedSet.popMin("worker5")).getValue();
                Object nextTarget = Objects.requireNonNull(redisSortedSet.range("worker5", 0, 0));

                return process(itemId, target, nextTarget);
            }
            default:
                return "Picker is NOT Present";
        }
    }

    @Override
    @Transactional
    public String changePosition(String pickerId) {
        Long pickerIdL = Long.parseLong(pickerId);
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

    private String process(long itemId, Object target, Object nextTarget){
        PickerRes targetPayload = mapper.convertValue(target, PickerRes.class);
        PickerRes nextTargetPayload = mapper.convertValue(nextTarget, PickerRes.class);

        // 지금 orderId와 다음 orderId가 같다면 같은 주문임
        if (targetPayload.getOrderId() == nextTargetPayload.getOrderId()) {
            ArrayList<ResponseItem> itemList = targetPayload.getResponseItemList();
            // 주문에 해당하는 item처리
            itemList.removeIf(item -> item.getId() == itemId);
            // 하나의 route 처리 완료 -> robotService로 출발 신호 보내기
            PickerReq pickedItem = PickerReq.builder()
                    .robotId(targetPayload.getRobotId())
                    .shelfId(targetPayload.getShelfId())
                    .responseItemList(itemList)
                    .build();
            kafkaProducer.pickedItem("PickerToRobot", pickedItem);
            return itemId + "is Picked";
        } // 다르다면 하나의 주문이 처리된 것 -> orderService로 complete 보냄
        else {
            ArrayList<ResponseItem> itemList = targetPayload.getResponseItemList();
            // 주문에 해당하는 item처리
            itemList.removeIf(item -> item.getId() == itemId);
            // 하나의 route 처리 완료 -> robotService로 출발 신호 보내기
            PickerReq pickedItem = PickerReq.builder()
                    .robotId(targetPayload.getRobotId())
                    .shelfId(targetPayload.getShelfId())
                    .build();
            kafkaProducer.pickedItem("PickerToRobot", pickedItem);

            PickerReq orderCompleted = PickerReq.builder()
                    .orderStatus(true)
                    .build();
            kafkaProducer.orderCompleted("PickerToOrder", orderCompleted);
            return itemId + "is Picked"
                    + "%n order no." + targetPayload.getOrderId() + "is Completed";
        }
    }
}
