package com.example.orderservice.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
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
    private String finishedTime;
    private int qty;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name="id")
    private OrderEntity orderEntity;
    @Builder
    public ItemEntity(Long stockId, OrderEntity orderEntity,int qty){
        this.status = false;
        this.stockId = stockId;
        this.orderEntity = orderEntity;
        this.qty = qty;
    }
}
