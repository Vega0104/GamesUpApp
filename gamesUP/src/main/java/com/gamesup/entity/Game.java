package com.gamesup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalDate releaseDate;

    @Column()
    private int stock;

    @Column(nullable = false)
    private float price;

    @Column(length = 3)
    private String currency;

    @ManyToOne
    @JoinColumn
    private Category category;

    @ManyToOne
    @JoinColumn
    private Publisher publisher;

    @ManyToOne
    @JoinColumn
    private Author author;

    @OneToMany(mappedBy = "game")
    private List<PurchaseLine> purchaseLines;

    @OneToMany(mappedBy = "game")
    private List<Review> reviews;

    @ManyToMany(mappedBy = "gamesWished")
    private List<User> usersWishing = new ArrayList<>();
}