package com.example.robotservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "shelf_stock")
public class ShelfStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long shelfStockId;
    private Long stockId;
    private String CreatedTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="shelf_id")
    @JsonBackReference
    private Shelf shelf;

}
