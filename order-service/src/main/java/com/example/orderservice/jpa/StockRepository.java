package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

public interface StockRepository extends CrudRepository<StockEntity, Long> {
}
