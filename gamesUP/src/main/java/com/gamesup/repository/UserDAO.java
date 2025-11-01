package com.gamesup.repository;

import com.gamesup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDAO extends JpaRepository<User, Long> {
    User findByEmailAndAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
}
