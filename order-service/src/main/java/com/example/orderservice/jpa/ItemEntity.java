package com.example.orderservice.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "items")
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;
    @Column(nullable = false)
    private Long stockId;
    @Column(nullable = false)
    private boolean status;
    private LocalDateTime finishedTime;
    private String name;
    private String image;
    private String about;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name="id")
    private OrderEntity orderEntity;
    @Builder
    public ItemEntity(Long stockId, OrderEntity orderEntity){
        this.status = false;
        this.stockId = stockId;
        this.orderEntity = orderEntity;
    }
}
