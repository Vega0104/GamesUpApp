package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Review;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.CUSTOMER);

        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");
        game.setSlug("test-game");
        game.setBasePrice(new BigDecimal("29.99"));
        game.setCurrency("EUR");

        review = new Review();
        review.setId(1L);
        review.setWrittenBy(user);
        review.setAbout(game);
        review.setRating(4);
        review.setComment("Great game!");
        review.setCreatedAt(LocalDateTime.now());
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_ShouldCreateReview_WhenValidData() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        // Act
        Review result = reviewService.create(1L, 1L, 5, "Excellent game!");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excellent game!", result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void create_ShouldCreateReview_WithoutComment() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Review result = reviewService.create(1L, 1L, 3, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getRating());
        assertNull(result.getComment());
    }

    @Test
    void create_ShouldTrimComment_WhenCommentHasWhitespace() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Review result = reviewService.create(1L, 1L, 4, "  Nice game  ");

        // Assert
        assertEquals("Nice game", result.getComment());
    }

    @Test
    void create_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(null, 1L, 4, "Comment")
        );
        assertEquals("Invalid user ID", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(1L, null, 4, "Comment")
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenRatingIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(1L, 1L, null, "Comment")
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenRatingIsTooLow() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(1L, 1L, 0, "Comment")
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenRatingIsTooHigh() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(1L, 1L, 6, "Comment")
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(99L, 1L, 4, "Comment")
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.create(1L, 99L, 4, "Comment")
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnReview_WhenIdExists() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // Act
        Optional<Review> result = reviewService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4, result.get().getRating());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Review> result = reviewService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.findById(null)
        );
        assertEquals("Invalid review ID", exception.getMessage());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.findById(0L)
        );
        assertEquals("Invalid review ID", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllReviews() {
        // Arrange
        Review review2 = new Review();
        review2.setId(2L);
        review2.setRating(5);
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review, review2));

        // Act
        List<Review> result = reviewService.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    // ========== FIND BY GAME TESTS ==========

    @Test
    void findByGame_ShouldReturnReviews_WhenGameExists() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.findByAboutOrderByCreatedAtDesc(game)).thenReturn(Arrays.asList(review));

        // Act
        List<Review> result = reviewService.findByGame(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Great game!", result.get(0).getComment());
    }

    @Test
    void findByGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.findByGame(99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void findByGame_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.findByGame(null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    // ========== FIND BY USER TESTS ==========

    @Test
    void findByUser_ShouldReturnReviews_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByWrittenBy(user)).thenReturn(Arrays.asList(review));

        // Act
        List<Review> result = reviewService.findByUser(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.findByUser(99L)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void findByUser_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.findByUser(null)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    // ========== GET AVERAGE RATING TESTS ==========

    @Test
    void getAverageRatingForGame_ShouldReturnAverage_WhenReviewsExist() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.calculateAverageRatingForGame(game)).thenReturn(4.5);

        // Act
        Double result = reviewService.getAverageRatingForGame(1L);

        // Assert
        assertEquals(4.5, result);
    }

    @Test
    void getAverageRatingForGame_ShouldReturnZero_WhenNoReviews() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.calculateAverageRatingForGame(game)).thenReturn(null);

        // Act
        Double result = reviewService.getAverageRatingForGame(1L);

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getAverageRatingForGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.getAverageRatingForGame(99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    // ========== GET REVIEW COUNT TESTS ==========

    @Test
    void getReviewCountForGame_ShouldReturnCount_WhenGameExists() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.countByAbout(game)).thenReturn(10L);

        // Act
        Long result = reviewService.getReviewCountForGame(1L);

        // Assert
        assertEquals(10L, result);
    }

    @Test
    void getReviewCountForGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.getReviewCountForGame(99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_ShouldUpdateReview_WhenValidData() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Review result = reviewService.update(1L, 5, "Updated comment");

        // Assert
        assertEquals(5, result.getRating());
        assertEquals("Updated comment", result.getComment());
    }

    @Test
    void update_ShouldThrowException_WhenReviewNotFound() {
        // Arrange
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.update(99L, 5, "Comment")
        );
        assertEquals("Review not found with id: 99", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenRatingIsInvalid() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.update(1L, 6, "Comment")
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeleteReview_WhenIdExists() {
        // Arrange
        when(reviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1L);

        // Act
        reviewService.delete(1L);

        // Assert
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenReviewNotFound() {
        // Arrange
        when(reviewRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.delete(99L)
        );
        assertEquals("Review not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.delete(null)
        );
        assertEquals("Invalid review ID", exception.getMessage());
    }
}