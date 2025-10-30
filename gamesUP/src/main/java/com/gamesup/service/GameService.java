package com.gamesup.service;

import com.gamesup.entity.Game;

import java.util.List;

public interface GameService {
    public List<Game> filter(String category, String author, String publisher, String name);

    public Game detail(long id);

    public void add(String title, float price, int stock, long authorID, long categoryID, long publisherID);

    public void add(Game game);

    public void remove(long id);

    public void update(Game game);

}
