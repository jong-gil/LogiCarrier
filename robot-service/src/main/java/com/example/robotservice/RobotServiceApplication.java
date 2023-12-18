package com.example.robotservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RobotServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RobotServiceApplication.class, args);
	}

}
