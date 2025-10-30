package com.gamesup.repository;

import com.gamesup.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewDAO extends JpaRepository<Review, Long> {
}
