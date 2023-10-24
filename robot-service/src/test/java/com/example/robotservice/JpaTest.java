package com.example.robotservice;

import com.example.robotservice.Repoistory.ShelfRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class JpaTest {
    @Autowired
    private ShelfRepository shelfRepository;
    @Test
    public void findSpacedShelf(){
        List<Long> shelfId = shelfRepository.findSpacedShelf(0);
        System.out.println(shelfId);
    }

}
