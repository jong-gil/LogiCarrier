package com.example.robotservice.jpa;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Data
@Table(name = "robot")
public class Robot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long robotId;
    private int positionX;
    private int positionY;
    @Column(nullable = true)
    private Long shelfId;
}
