package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Review;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByAbout(Game game);

    List<Review> findByWrittenBy(User user);

    List<Review> findByAboutOrderByCreatedAtDesc(Game game);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.about = :game")
    Double calculateAverageRatingForGame(Game game);

    Long countByAbout(Game game);
}