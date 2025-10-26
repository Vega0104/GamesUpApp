package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByOwnedBy(User user);

    boolean existsByOwnedBy(User user);
}