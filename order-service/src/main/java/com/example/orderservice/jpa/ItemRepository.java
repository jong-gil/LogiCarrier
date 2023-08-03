package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<ItemEntity, Long> {
    List<ItemEntity> findAllByOrderEntity(OrderEntity orderEntity);
}
