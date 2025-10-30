package com.gamesup.repository;

import com.gamesup.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameDAO extends JpaRepository<Game, Long> {
    public List<Game> findByCategoryNameOrAuthorNameOrPublisherNameOrTitle(String category, String author, String publisher, String title);
}
