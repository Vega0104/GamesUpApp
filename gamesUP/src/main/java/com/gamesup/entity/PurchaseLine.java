package com.gamesup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column
    private float unitPriceAtPurchase;



    @ManyToOne
    @JoinColumn(nullable = false)
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Game game;
}