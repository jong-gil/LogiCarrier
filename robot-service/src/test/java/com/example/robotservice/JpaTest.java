package com.example.robotservice;

import com.example.robotservice.Repoistory.ShelfRepository;
import com.example.robotservice.dto.SpaceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.util.List;
@SpringBootTest
public class JpaTest {
    @Autowired
    private ShelfRepository shelfRepository;
    @Test
    public void findSpacedShelf(){
        List<Object[]> objects = shelfRepository.findSpacedShelf();
        for (Object obj[] : objects) {
            SpaceDto spaceDto = new SpaceDto().fromObj(obj);
            System.out.println(spaceDto);
        }
    }

}
