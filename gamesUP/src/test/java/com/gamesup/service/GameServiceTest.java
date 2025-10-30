package com.gamesup.service;

import com.gamesup.entity.Game;
import com.gamesup.repository.GameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional  // Rollback après chaque test
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameDAO gameDAO;

    @BeforeEach
    public void setUpTest(){
        gameDAO.deleteAll();
    }

    @Test
    public void addNewGameOK(){
        gameService.add("Monopoly", 20, 2, 1, 1, 1);
        assertEquals(1, gameDAO.findAll().size());
        gameService.add("Code Name", 30, 2, 2, 2, 2);
        assertEquals(2, gameDAO.findAll().size());
        
    }

    @Test
    public void filterOK(){
        // Attention : ce test suppose que la DB contient déjà 2 jeux
        // Il vaudrait mieux initialiser les données dans le test

        gameService.add("Monopoly", 20, 2, 1, 1, 1);
        gameService.add("Code Name", 30, 2, 2, 2, 2);
        assertEquals(2, gameDAO.findAll().size());

        List<Game> games = gameService.filter("Aventure", null, null, null);
        assertEquals(0, games.size());

        // Bug ici : tu réassignes games mais tu testes l'ancienne variable
        games = gameService.filter("Action", "Lou", "Enix", "Street Fighter");
        assertEquals(1, games.size());
    }


}