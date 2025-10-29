package com.gamesup.repository;

import com.gamesup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, Long> {
    User findByEmailAndAndPassword(String email, String password);
}
