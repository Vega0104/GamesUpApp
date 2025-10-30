package com.gamesup.repository;

import com.gamesup.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryDAO extends JpaRepository<Category, Long> {
}
