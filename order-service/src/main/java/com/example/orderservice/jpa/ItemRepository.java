package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<ItemEntity, Long> {
}
