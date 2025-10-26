package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByPlacedBy(User user);

    List<Purchase> findByPlacedByOrderByCreatedAtDesc(User user);

    List<Purchase> findByStatus(OrderStatus status);

    List<Purchase> findByPlacedByAndStatus(User user, OrderStatus status);
}