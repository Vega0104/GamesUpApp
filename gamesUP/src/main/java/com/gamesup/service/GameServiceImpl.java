package com.gamesup.service;

import com.gamesup.entity.Game;
import com.gamesup.repository.AuthorDAO;
import com.gamesup.repository.CategoryDAO;
import com.gamesup.repository.GameDAO;
import com.gamesup.repository.PublisherDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService{

    @Autowired
    private PublisherDAO publisherDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private GameDAO gameDAO;

    @Autowired
    private AuthorDAO authorDAO;

    @Override
    public List<Game> filter(String category, String author, String publisher, String name) {
        return this.gameDAO.findByCategoryNameOrAuthorNameOrPublisherNameOrTitle(category,author,publisher,name);
    }

    @Override
    public Game detail(long id) {
        return this.gameDAO.findById(id).get();
    }

    @Override
    public void add(String title, float price, int stock, long authorID, long categoryID, long publisherID) {
        Game game = new Game();
        game.setTitle(title);
        game.setPrice(price);
        game.setStock(stock);
        game.setAuthor(authorDAO.getReferenceById(authorID));
        game.setCategory(categoryDAO.getReferenceById(categoryID));
        game.setPublisher(publisherDAO.getReferenceById(publisherID));
        this.gameDAO.save(game);
    }

    @Override
    public void add(Game game) {
        this.gameDAO.save(game);
    }

    @Override
    public void remove(long id) {
        this.gameDAO.deleteById(id);
    }

    @Override
//    @PreAuthorize("hasRole('ADMIN')")
    public void update(long id,String title, float price, int stock, long authorID, long categoryID, long publisherID) {

        // recup le game par son id
        Game game = gameDAO.findById(id).get();

        // set sur mes champs
        game.setTitle(title);
        game.setPrice(price);
        game.setStock(stock);
        game.setAuthor(authorDAO.getReferenceById(authorID));
        game.setCategory(categoryDAO.getReferenceById(categoryID));
        game.setPublisher(publisherDAO.getReferenceById(publisherID));
        // save
        this.gameDAO.save(game);
    }
}
