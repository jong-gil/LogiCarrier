package com.example.robotservice.jpa;

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
    private int assignment;
    private Long userId;
    private int x;
    private int y;
}
