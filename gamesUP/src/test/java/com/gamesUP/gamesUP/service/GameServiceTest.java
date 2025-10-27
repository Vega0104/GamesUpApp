package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private GameService gameService;

    private Game game;
    private Category category;
    private Publisher publisher;
    private Author author;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Action");
        category.setSlug("action");

        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Asmodée");

        author = new Author();
        author.setId(1L);
        author.setName("Antoine Bauza");

        game = new Game();
        game.setId(1L);
        game.setTitle("7 Wonders");
        game.setSlug("7-wonders");
        game.setDescription("Strategy board game");
        game.setReleaseDate(LocalDate.of(2010, 6, 1));
        game.setBasePrice(new BigDecimal("39.99"));
        game.setCurrency("EUR");
        game.setCategorizedAs(category);
        game.setPublishedBy(publisher);
        game.setCreatedBy(author);
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_ShouldCreateGame_WhenValidData() {
        // Arrange
        when(gameRepository.existsBySlug("catan")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game g = invocation.getArgument(0);
            g.setId(2L);
            return g;
        });

        // Act
        Game result = gameService.create("Catan", "Resource management game", LocalDate.of(1995, 1, 1),
                new BigDecimal("44.99"), "EUR", 1L, 1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Catan", result.getTitle());
        assertEquals("catan", result.getSlug());
        assertEquals(new BigDecimal("44.99"), result.getBasePrice());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void create_ShouldCreateGame_WithoutOptionalFields() {
        // Arrange
        when(gameRepository.existsBySlug("simple-game")).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Game result = gameService.create("Simple Game", null, null,
                new BigDecimal("9.99"), "EUR", null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals("Simple Game", result.getTitle());
        assertNull(result.getCategorizedAs());
        assertNull(result.getPublishedBy());
        assertNull(result.getCreatedBy());
    }

    @Test
    void create_ShouldThrowException_WhenTitleIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create(null, "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, null, null)
        );
        assertEquals("Game title cannot be null or empty", exception.getMessage());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenTitleIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("   ", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, null, null)
        );
        assertEquals("Game title cannot be null or empty", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenPriceIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("Game", "desc", LocalDate.now(),
                        null, "EUR", null, null, null)
        );
        assertEquals("Base price must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenPriceIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("Game", "desc", LocalDate.now(),
                        new BigDecimal("-10"), "EUR", null, null, null)
        );
        assertEquals("Base price must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenSlugAlreadyExists() {
        // Arrange
        when(gameRepository.existsBySlug("existing-game")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("Existing Game", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, null, null)
        );
        assertEquals("Game with similar title already exists", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        when(gameRepository.existsBySlug(any())).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("Game", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", 99L, null, null)
        );
        assertEquals("Category not found with id: 99", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenPublisherNotFound() {
        // Arrange
        when(gameRepository.existsBySlug(any())).thenReturn(false);
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("Game", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, 99L, null)
        );
        assertEquals("Publisher not found with id: 99", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenAuthorNotFound() {
        // Arrange
        when(gameRepository.existsBySlug(any())).thenReturn(false);
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.create("Game", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, null, 99L)
        );
        assertEquals("Author not found with id: 99", exception.getMessage());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnGame_WhenIdExists() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        // Act
        Optional<Game> result = gameService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("7 Wonders", result.get().getTitle());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Game> result = gameService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findById(null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findById(0L)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    // ========== FIND BY SLUG TESTS ==========

    @Test
    void findBySlug_ShouldReturnGame_WhenSlugExists() {
        // Arrange
        when(gameRepository.findBySlug("7-wonders")).thenReturn(Optional.of(game));

        // Act
        Optional<Game> result = gameService.findBySlug("7-wonders");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("7 Wonders", result.get().getTitle());
    }

    @Test
    void findBySlug_ShouldThrowException_WhenSlugIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findBySlug(null)
        );
        assertEquals("Slug cannot be null or empty", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllGames() {
        // Arrange
        Game game2 = new Game();
        game2.setId(2L);
        game2.setTitle("Catan");
        when(gameRepository.findAll()).thenReturn(Arrays.asList(game, game2));

        // Act
        List<Game> result = gameService.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    // ========== FIND BY TITLE TESTS ==========

    @Test
    void findByTitle_ShouldReturnGames_WhenTitleMatches() {
        // Arrange
        when(gameRepository.findByTitleContainingIgnoreCase("wonders")).thenReturn(Arrays.asList(game));

        // Act
        List<Game> result = gameService.findByTitle("wonders");

        // Assert
        assertEquals(1, result.size());
        assertEquals("7 Wonders", result.get(0).getTitle());
    }

    @Test
    void findByTitle_ShouldThrowException_WhenTitleIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findByTitle(null)
        );
        assertEquals("Title cannot be null or empty", exception.getMessage());
    }

    // ========== FIND BY CATEGORY TESTS ==========

    @Test
    void findByCategory_ShouldReturnGames_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(gameRepository.findByCategorizedAs(category)).thenReturn(Arrays.asList(game));

        // Act
        List<Game> result = gameService.findByCategory(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByCategory_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findByCategory(99L)
        );
        assertEquals("Category not found with id: 99", exception.getMessage());
    }

    @Test
    void findByCategory_ShouldThrowException_WhenCategoryIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findByCategory(null)
        );
        assertEquals("Invalid category ID", exception.getMessage());
    }

    // ========== FIND BY PUBLISHER TESTS ==========

    @Test
    void findByPublisher_ShouldReturnGames_WhenPublisherExists() {
        // Arrange
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(gameRepository.findByPublishedBy(publisher)).thenReturn(Arrays.asList(game));

        // Act
        List<Game> result = gameService.findByPublisher(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByPublisher_ShouldThrowException_WhenPublisherNotFound() {
        // Arrange
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findByPublisher(99L)
        );
        assertEquals("Publisher not found with id: 99", exception.getMessage());
    }

    // ========== FIND BY AUTHOR TESTS ==========

    @Test
    void findByAuthor_ShouldReturnGames_WhenAuthorExists() {
        // Arrange
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(gameRepository.findByCreatedBy(author)).thenReturn(Arrays.asList(game));

        // Act
        List<Game> result = gameService.findByAuthor(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByAuthor_ShouldThrowException_WhenAuthorNotFound() {
        // Arrange
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.findByAuthor(99L)
        );
        assertEquals("Author not found with id: 99", exception.getMessage());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_ShouldUpdateGame_WhenValidData() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.existsBySlug("7-wonders-updated")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Game result = gameService.update(1L, "7 Wonders Updated", "New desc",
                LocalDate.of(2011, 1, 1), new BigDecimal("49.99"), "EUR",
                1L, 1L, 1L);

        // Assert
        assertEquals("7 Wonders Updated", result.getTitle());
        assertEquals(new BigDecimal("49.99"), result.getBasePrice());
    }

    @Test
    void update_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.update(99L, "Title", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, null, null)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenNewSlugAlreadyExists() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.existsBySlug("new-game")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.update(1L, "New Game", "desc", LocalDate.now(),
                        new BigDecimal("10"), "EUR", null, null, null)
        );
        assertEquals("Game with similar title already exists", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeleteGame_WhenIdExists() {
        // Arrange
        when(gameRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gameRepository).deleteById(1L);

        // Act
        gameService.delete(1L);

        // Assert
        verify(gameRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.delete(99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.delete(null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }
}