package com.example.robotservice.jpa;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "shelf")
public class Shelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long shelfId;
    private int x;
    private int y;
    private Boolean status;

    @OneToMany(mappedBy = "shelf", fetch = FetchType.LAZY)
    private List<ShelfStock> shelfStockList;
}
