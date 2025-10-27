package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findBySlug(String slug);

    List<Game> findByTitleContainingIgnoreCase(String title);

    List<Game> findByCategorizedAs(Category category);

    List<Game> findByPublishedBy(Publisher publisher);

    List<Game> findByCreatedBy(Author author);

    boolean existsBySlug(String slug);
}