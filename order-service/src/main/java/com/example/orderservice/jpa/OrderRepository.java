package com.example.orderservice.jpa;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrderRepository extends CrudRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByStatusOrderByCreatedTimeDesc(int status);
}
