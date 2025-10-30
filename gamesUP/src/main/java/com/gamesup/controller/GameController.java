package com.gamesup.controller;

import com.gamesup.entity.Game;
import com.gamesup.repository.GameDAO;
import com.gamesup.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping(path = "/add")
    public void add( @RequestParam(required = false) String title,
                     @RequestParam(required = false) float price,
                     @RequestParam(required = false) int stock,
                     @RequestParam(required = false) long authorID,
                     @RequestParam(required = false) long categoryID,
                     @RequestParam(required = false) long publisherID){
        gameService.add(title, price,stock,authorID,categoryID,publisherID);
    }

    @GetMapping(path = "/filter")
    public List<Game> filter(  @RequestParam(required = false) String category,
                                @RequestParam(required = false) String author,
                                @RequestParam(required = false) String publisher,
                                @RequestParam(required = false) String name) {
        return gameService.filter(category, author, publisher, name);
    }
}
