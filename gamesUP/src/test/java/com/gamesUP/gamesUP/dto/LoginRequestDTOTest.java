package com.gamesUP.gamesUP.dto.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidate_WhenAllFieldsAreValid() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO(
                "john@example.com",
                "password123"
        );

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidation_WhenEmailIsBlank() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO("", "password123");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    void shouldFailValidation_WhenEmailIsInvalid() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO("not-an-email", "password123");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    void shouldFailValidation_WhenPasswordIsBlank() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO("john@example.com", "");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }
}