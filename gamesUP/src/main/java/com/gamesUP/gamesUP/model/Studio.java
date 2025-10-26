package com.gamesUP.gamesUP.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "studios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Studio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;
}

// remarques
// on prefera utiliser un studio plutot qu'un auteur
// Lister<Game> games n'est pas necessaire dans Author car on peut acceder aux jeux via l'entite Game
// On utilisera cela dans le repository si besoin d'une liste de jeux par auteur