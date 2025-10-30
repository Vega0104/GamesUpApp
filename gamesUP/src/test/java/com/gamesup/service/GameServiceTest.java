package com.gamesup.service;

import com.gamesup.entity.Game;
import com.gamesup.repository.GameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameDAO gameDAO;

    @Test
    public void detailOK() {
        Game game = gameService.detail(41);
    }

    @Test
    public void filterOK() {
        gameService.filter("Réflexion", "Lou", "Ubi Soft", "Dracula");
    }

    @Test
    public void addOk() {
        int countBefore = gameDAO.findAll().size();
        gameService.add("Code Name", 12, 20, 1, 1, 1);

        int countAfter = gameDAO.findAll().size();
        assertEquals(countBefore + 1, countAfter);
    }

    @Test
    public void deleteOK() {
        gameService.remove(89);
    }

    @Test
    public void updateOK() {
        gameService.update(90, "Code Number",13, 5, 2,2,1);
    }
}