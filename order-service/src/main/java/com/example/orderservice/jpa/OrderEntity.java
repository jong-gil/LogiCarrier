package com.example.orderservice.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jdk.jfr.DataAmount;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;

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
    @Column(nullable = true)
    private LocalDateTime finishedTime;
    private Long userId;

    @Builder
    public OrderEntity(){
        this.status = 0;
        this.createdTime = LocalDateTime.now();
    }
}
