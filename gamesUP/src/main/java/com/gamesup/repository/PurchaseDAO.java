package com.gamesup.repository;

import com.gamesup.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseDAO extends JpaRepository<Purchase, Long> {
    public List<Purchase> findByUserIdAndStatusOrderByCreatedAtDesc(long userID, Purchase.OrderStatus orderStatus);
}
