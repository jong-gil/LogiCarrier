package com.example.orderservice.jpa;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="stocks")
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long id;
    private String name;
    private String about;
    private String image;
    private int amount;

}
