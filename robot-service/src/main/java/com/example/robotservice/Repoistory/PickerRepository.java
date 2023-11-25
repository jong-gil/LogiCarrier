package com.example.robotservice.Repoistory;

import com.example.robotservice.entity.Picker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickerRepository extends JpaRepository<Picker, Long> {
}
