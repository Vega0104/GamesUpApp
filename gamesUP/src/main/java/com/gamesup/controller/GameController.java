package com.gamesup.controller;

import com.gamesup.entity.Game;
import com.gamesup.repository.GameDAO;
import com.gamesup.service.GameService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @PutMapping(path = "/game/update")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@RequestParam long id,
                       @RequestParam(required = false) String title,
                       @RequestParam(required = false) float price,
                       @RequestParam(required = false) int stock,
                       @RequestParam(required = false) long authorID,
                       @RequestParam(required = false) long categoryID,
                       @RequestParam(required = false) long publisherID) {
        gameService.update(id, title, price, stock,authorID,categoryID, publisherID);
    }

    @DeleteMapping(path = "/game/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@RequestParam long id) {
        gameService.remove(id);
    }


    @GetMapping(path = "/game/details")
    public Game detail(@RequestParam long gameID) {
        Game game = gameService.detail(gameID);
        return game;
    }


    @PostMapping(path = "/game/add")
    @PreAuthorize("hasRole('ADMIN')")
    public void add( @RequestParam(required = false) String title,
                     @RequestParam(required = false) float price,
                     @RequestParam(required = false) int stock,
                     @RequestParam(required = false) long authorID,
                     @RequestParam(required = false) long categoryID,
                     @RequestParam(required = false) long publisherID){
        gameService.add(title, price,stock,authorID,categoryID,publisherID);
    }

    @GetMapping(path = "/game/filter")
    public List<Game> filter(  @RequestParam(required = false) String category,
                                @RequestParam(required = false) String author,
                                @RequestParam(required = false) String publisher,
                                @RequestParam(required = false) String name) {
        return gameService.filter(category, author, publisher, name);
    }


}
