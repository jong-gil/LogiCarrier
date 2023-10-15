package com.example.robotservice;

import com.example.robotservice.Repoistory.ShelfStockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RobotServiceApplicationTests {
	@Autowired
	ShelfStockRepository stockRepository;
	@Test
	void contextLoads() {
	}

}
