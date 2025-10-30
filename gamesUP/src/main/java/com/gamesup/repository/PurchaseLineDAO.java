package com.gamesup.repository;

import com.gamesup.entity.PurchaseLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseLineDAO extends JpaRepository<PurchaseLine, Long> {
}
