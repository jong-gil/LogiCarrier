package com.example.workerservice.repository;

import com.example.workerservice.entity.WorkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<WorkerEntity, Long> {
    Optional<WorkerEntity> findByWorkerId(Long workerId);
}
