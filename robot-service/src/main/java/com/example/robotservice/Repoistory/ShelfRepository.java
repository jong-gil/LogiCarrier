package com.example.robotservice.Repoistory;

import com.example.robotservice.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    @Query(value = "select distinct s from Shelf s left join fetch s.shelfStockList")
    List<Shelf> findBestStock();
}
