package com.example.robotservice.Repoistory;

import com.example.robotservice.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    @Query(value = "select s.id from Shelf s left join ShelfStock t on s.id = t.shelf.id " +
            "group by s.id order by count(s.id) asc, abs(s.x - :x) desc")
    List<Long> findSpacedShelf(@Param("x") int x);              //가까운 선반 부터 찾기
}
