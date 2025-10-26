package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long> {

    Optional<Studio> findByName(String name);

    boolean existsByName(String name);
}