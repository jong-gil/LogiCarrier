package com.example.robotservice.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "picker")
public class Picker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long pickerId;
    private Long userId;
    private int x;
    private int y;
}
