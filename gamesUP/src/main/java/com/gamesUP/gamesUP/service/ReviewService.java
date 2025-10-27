package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Review;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         GameRepository gameRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    public Review create(Long userId, Long gameId, Integer rating, String comment) {
        // Validations
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Vérifier que l'utilisateur et le jeu existent
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        // Créer l'avis
        Review review = new Review();
        review.setWrittenBy(user);
        review.setAbout(game);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);

        return reviewRepository.save(review);
    }

    public Optional<Review> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        return reviewRepository.findById(id);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public List<Review> findByGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        return reviewRepository.findByAboutOrderByCreatedAtDesc(game);
    }

    public List<Review> findByUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return reviewRepository.findByWrittenBy(user);
    }

    public Double getAverageRatingForGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        Double average = reviewRepository.calculateAverageRatingForGame(game);
        return average != null ? average : 0.0;
    }

    public Long getReviewCountForGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        return reviewRepository.countByAbout(game);
    }

    public Review update(Long id, Integer rating, String comment) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);

        return reviewRepository.save(review);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }

        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found with id: " + id);
        }

        reviewRepository.deleteById(id);
    }
}