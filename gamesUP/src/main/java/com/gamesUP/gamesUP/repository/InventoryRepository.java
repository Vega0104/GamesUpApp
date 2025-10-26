package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Inventory;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySku(String sku);

    List<Inventory> findByProduct(Game game);

    List<Inventory> findByProductAndPlatform(Game game, Platform platform);

    List<Inventory> findByActiveTrue();

    List<Inventory> findByStockQuantityGreaterThan(Integer quantity);

    boolean existsBySku(String sku);
}