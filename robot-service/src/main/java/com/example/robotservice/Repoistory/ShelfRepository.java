package com.example.robotservice.Repoistory;

import com.example.robotservice.dto.SpaceDto;
import com.example.robotservice.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;
import java.util.List;
import java.util.Optional;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    @Query(value = "select s.id as shelfId, 4 - round(sum(IFNULL(t.shelf_id, 0)/s.id), 0) as qty, s.x as x, s.y as y from Shelf s left join Shelf_Stock t on s.id = t.shelf_id " +
            "group by s.id order by count(s.id) asc, abs(s.x - :x) desc", nativeQuery = true)
    List<Object[]> findSpacedShelf(@Param("x") int x);              //가까운 선반 부터 찾기
}
