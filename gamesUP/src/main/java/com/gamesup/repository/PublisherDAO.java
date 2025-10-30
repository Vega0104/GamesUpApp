package com.gamesup.repository;

import com.gamesup.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherDAO extends JpaRepository<Publisher, Long> {
}
