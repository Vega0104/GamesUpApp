package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.repository.WishlistRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private User user;
    private Game game;
    private Wishlist wishlist;

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

        wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setOwnedBy(user);
        wishlist.setGames(new ArrayList<>());
    }

    // ========== GET OR CREATE WISHLIST TESTS ==========

    @Test
    void getOrCreateWishlist_ShouldReturnExistingWishlist_WhenWishlistExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));

        // Act
        Wishlist result = wishlistService.getOrCreateWishlist(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void getOrCreateWishlist_ShouldCreateNewWishlist_WhenWishlistDoesNotExist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist w = invocation.getArgument(0);
            w.setId(2L);
            return w;
        });

        // Act
        Wishlist result = wishlistService.getOrCreateWishlist(1L);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getOwnedBy());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void getOrCreateWishlist_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.getOrCreateWishlist(null)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void getOrCreateWishlist_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.getOrCreateWishlist(99L)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    // ========== ADD GAME TESTS ==========

    @Test
    void addGame_ShouldAddGame_WhenGameNotInWishlist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Wishlist result = wishlistService.addGame(1L, 1L);

        // Assert
        assertTrue(result.getGames().contains(game));
        assertEquals(1, result.getGames().size());
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void addGame_ShouldCreateWishlistAndAddGame_WhenWishlistDoesNotExist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist w = invocation.getArgument(0);
            w.setId(2L);
            return w;
        });

        // Act
        Wishlist result = wishlistService.addGame(1L, 1L);

        // Assert
        assertTrue(result.getGames().contains(game));
        verify(wishlistRepository, times(2)).save(any(Wishlist.class)); // Once for creation, once for adding game
    }

    @Test
    void addGame_ShouldThrowException_WhenGameAlreadyInWishlist() {
        // Arrange
        wishlist.getGames().add(game);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.addGame(1L, 1L)
        );
        assertEquals("Game is already in the wishlist", exception.getMessage());
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void addGame_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.addGame(null, 1L)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void addGame_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.addGame(1L, null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void addGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.addGame(1L, 99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    // ========== REMOVE GAME TESTS ==========

    @Test
    void removeGame_ShouldRemoveGame_WhenGameInWishlist() {
        // Arrange
        wishlist.getGames().add(game);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Wishlist result = wishlistService.removeGame(1L, 1L);

        // Assert
        assertFalse(result.getGames().contains(game));
        assertEquals(0, result.getGames().size());
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void removeGame_ShouldThrowException_WhenGameNotInWishlist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.removeGame(1L, 1L)
        );
        assertEquals("Game is not in the wishlist", exception.getMessage());
    }

    @Test
    void removeGame_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.removeGame(null, 1L)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void removeGame_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.removeGame(1L, null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void removeGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.removeGame(1L, 99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    // ========== CONTAINS GAME TESTS ==========

    @Test
    void containsGame_ShouldReturnTrue_WhenGameInWishlist() {
        // Arrange
        wishlist.getGames().add(game);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));

        // Act
        boolean result = wishlistService.containsGame(1L, 1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void containsGame_ShouldReturnFalse_WhenGameNotInWishlist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));

        // Act
        boolean result = wishlistService.containsGame(1L, 1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void containsGame_ShouldReturnFalse_WhenWishlistDoesNotExist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.empty());

        // Act
        boolean result = wishlistService.containsGame(1L, 1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void containsGame_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.containsGame(null, 1L)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void containsGame_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.containsGame(1L, null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void containsGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.containsGame(1L, 99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void containsGame_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.containsGame(99L, 1L)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    // ========== CLEAR WISHLIST TESTS ==========

    @Test
    void clearWishlist_ShouldClearAllGames() {
        // Arrange
        wishlist.getGames().add(game);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByOwnedBy(user)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        wishlistService.clearWishlist(1L);

        // Assert
        assertTrue(wishlist.getGames().isEmpty());
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void clearWishlist_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.clearWishlist(null)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void clearWishlist_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.clearWishlist(99L)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }
}