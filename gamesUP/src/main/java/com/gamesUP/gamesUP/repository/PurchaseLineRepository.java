package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.PurchaseLine;
import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseLineRepository extends JpaRepository<PurchaseLine, Long> {

    List<PurchaseLine> findByPurchase(Purchase purchase);

    List<PurchaseLine> findByItem(Game game);
}