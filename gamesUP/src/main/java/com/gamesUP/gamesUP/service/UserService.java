package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String fullName, String email, String password, LocalDateTime birthDate) {
        // Validations
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        String trimmedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Créer l'utilisateur
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(trimmedEmail);
        user.setPasswordHash(hashPassword(password));
        user.setBirthDate(birthDate);
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    public User createAdmin(String fullName, String email, String password) {
        // Validations
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        String trimmedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Créer l'admin
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(trimmedEmail);
        user.setPasswordHash(hashPassword(password));
        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return userRepository.findByRole(role);
    }

    public User update(Long id, String fullName, String email, LocalDateTime birthDate) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        String trimmedEmail = email.trim().toLowerCase();

        // Vérifier si le nouvel email existe déjà (sauf si c'est le même utilisateur)
        if (!user.getEmail().equals(trimmedEmail) && userRepository.existsByEmail(trimmedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setFullName(fullName.trim());
        user.setEmail(trimmedEmail);
        user.setBirthDate(birthDate);

        return userRepository.save(user);
    }

    public User changePassword(Long id, String oldPassword, String newPassword) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new IllegalArgumentException("Old password cannot be null or empty");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Vérifier l'ancien mot de passe
        if (!verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPasswordHash(hashPassword(newPassword));

        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    // Méthodes utilitaires privées

    private String hashPassword(String password) {
        // Simulation simple - en production, utiliser BCrypt
        return "HASHED_" + password;
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Simulation simple - en production, utiliser BCrypt
        return hashedPassword.equals("HASHED_" + plainPassword);
    }

    private boolean isValidEmail(String email) {
        // Validation simple - en production, utiliser une regex plus robuste
        return email != null && email.contains("@") && email.contains(".");
    }
}