package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;

    public GameService(GameRepository gameRepository,
                       CategoryRepository categoryRepository,
                       PublisherRepository publisherRepository,
                       AuthorRepository authorRepository) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
    }

    public Game create(String title, String description, LocalDate releaseDate,
                       BigDecimal basePrice, String currency,
                       Long categoryId, Long publisherId, Long authorId) {

        // Validations
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Game title cannot be null or empty");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price must be greater than or equal to 0");
        }

        String trimmedTitle = title.trim();

        // Vérifier si le jeu existe déjà
        if (gameRepository.existsBySlug(generateSlug(trimmedTitle))) {
            throw new IllegalArgumentException("Game with similar title already exists");
        }

        // Récupérer les entités liées
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        }

        Publisher publisher = null;
        if (publisherId != null) {
            publisher = publisherRepository.findById(publisherId)
                    .orElseThrow(() -> new IllegalArgumentException("Publisher not found with id: " + publisherId));
        }

        Author author = null;
        if (authorId != null) {
            author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + authorId));
        }

        // Créer le jeu
        Game game = new Game();
        game.setTitle(trimmedTitle);
        game.setSlug(generateSlug(trimmedTitle));
        game.setDescription(description);
        game.setReleaseDate(releaseDate);
        game.setBasePrice(basePrice);
        game.setCurrency(currency);
        game.setCategorizedAs(category);
        game.setPublishedBy(publisher);
        game.setCreatedBy(author);

        return gameRepository.save(game);
    }

    public Optional<Game> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        return gameRepository.findById(id);
    }

    public Optional<Game> findBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }
        return gameRepository.findBySlug(slug.trim());
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    public List<Game> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        return gameRepository.findByTitleContainingIgnoreCase(title.trim());
    }

    public List<Game> findByCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        return gameRepository.findByCategorizedAs(category);
    }

    public List<Game> findByPublisher(Long publisherId) {
        if (publisherId == null || publisherId <= 0) {
            throw new IllegalArgumentException("Invalid publisher ID");
        }
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found with id: " + publisherId));
        return gameRepository.findByPublishedBy(publisher);
    }

    public List<Game> findByAuthor(Long authorId) {
        if (authorId == null || authorId <= 0) {
            throw new IllegalArgumentException("Invalid author ID");
        }
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + authorId));
        return gameRepository.findByCreatedBy(author);
    }

    public Game update(Long id, String title, String description, LocalDate releaseDate,
                       BigDecimal basePrice, String currency,
                       Long categoryId, Long publisherId, Long authorId) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Game title cannot be null or empty");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price must be greater than or equal to 0");
        }

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + id));

        String trimmedTitle = title.trim();
        String newSlug = generateSlug(trimmedTitle);

        // Vérifier si le nouveau slug existe déjà (sauf si c'est le même jeu)
        if (!game.getSlug().equals(newSlug) && gameRepository.existsBySlug(newSlug)) {
            throw new IllegalArgumentException("Game with similar title already exists");
        }

        // Récupérer les entités liées
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        }

        Publisher publisher = null;
        if (publisherId != null) {
            publisher = publisherRepository.findById(publisherId)
                    .orElseThrow(() -> new IllegalArgumentException("Publisher not found with id: " + publisherId));
        }

        Author author = null;
        if (authorId != null) {
            author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + authorId));
        }

        // Mettre à jour
        game.setTitle(trimmedTitle);
        game.setSlug(newSlug);
        game.setDescription(description);
        game.setReleaseDate(releaseDate);
        game.setBasePrice(basePrice);
        game.setCurrency(currency);
        game.setCategorizedAs(category);
        game.setPublishedBy(publisher);
        game.setCreatedBy(author);

        return gameRepository.save(game);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }

        gameRepository.deleteById(id);
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ïî]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}