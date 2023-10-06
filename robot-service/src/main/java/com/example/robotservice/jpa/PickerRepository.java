package com.example.robotservice.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickerRepository extends JpaRepository<Picker, Long> {
    List<Picker> findByAssignmentLessThan(int assignment);
}
