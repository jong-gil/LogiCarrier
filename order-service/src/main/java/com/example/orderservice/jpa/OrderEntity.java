package com.example.orderservice.jpa;

import jdk.jfr.DataAmount;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer status;
    @Column(nullable = false)
    private LocalDateTime createdTime;
    @Column(nullable = false)
    private LocalDateTime finishedTime;
    @Column(nullable = false)
    private Long userId;

}
