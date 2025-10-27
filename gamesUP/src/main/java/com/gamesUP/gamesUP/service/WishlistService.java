package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.WishlistRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           GameRepository gameRepository,
                           UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    public Wishlist getOrCreateWishlist(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Optional<Wishlist> existingWishlist = wishlistRepository.findByOwnedBy(user);

        if (existingWishlist.isPresent()) {
            return existingWishlist.get();
        }

        // Créer une nouvelle wishlist
        Wishlist wishlist = new Wishlist();
        wishlist.setOwnedBy(user);
        wishlist.setGames(new ArrayList<>());

        return wishlistRepository.save(wishlist);
    }

    public Wishlist addGame(Long userId, Long gameId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        Wishlist wishlist = getOrCreateWishlist(userId);

        // Vérifier si le jeu n'est pas déjà dans la wishlist
        if (wishlist.getGames().contains(game)) {
            throw new IllegalArgumentException("Game is already in the wishlist");
        }

        wishlist.getGames().add(game);

        return wishlistRepository.save(wishlist);
    }

    public Wishlist removeGame(Long userId, Long gameId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        Wishlist wishlist = getOrCreateWishlist(userId);

        if (!wishlist.getGames().contains(game)) {
            throw new IllegalArgumentException("Game is not in the wishlist");
        }

        wishlist.getGames().remove(game);

        return wishlistRepository.save(wishlist);
    }

    public boolean containsGame(Long userId, Long gameId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Optional<Wishlist> wishlist = wishlistRepository.findByOwnedBy(user);

        return wishlist.isPresent() && wishlist.get().getGames().contains(game);
    }

    public void clearWishlist(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Wishlist wishlist = getOrCreateWishlist(userId);
        wishlist.getGames().clear();

        wishlistRepository.save(wishlist);
    }
}