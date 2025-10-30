package com.gamesup.repository;

import com.gamesup.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorDAO extends JpaRepository<Author, Long> {
}
