package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StockRepository extends CrudRepository<StockEntity, Long> {
    Optional<StockEntity> findById(Long id);
}
