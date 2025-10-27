package com.gamesUP.gamesUP.dto.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SignupRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidate_WhenAllFieldsAreValid() {
        // Arrange
        SignupRequestDTO dto = new SignupRequestDTO(
                "John Doe",
                "john@example.com",
                "password123"
        );

        // Act
        Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidation_WhenFullNameIsBlank() {
        // Arrange
        SignupRequestDTO dto = new SignupRequestDTO(
                "",
                "john@example.com",
                "password123"
        );

        // Act
        Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        // @NotBlank ET @Size peuvent tous les deux échouer
        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Full name")));
    }

    @Test
    void shouldFailValidation_WhenFullNameIsTooShort() {
        // Arrange
        SignupRequestDTO dto = new SignupRequestDTO(
                "J",
                "john@example.com",
                "password123"
        );

        // Act
        Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 2 and 100 characters")));
    }

    @Test
    void shouldFailValidation_WhenEmailIsInvalid() {
        // Arrange
        SignupRequestDTO dto = new SignupRequestDTO(
                "John Doe",
                "invalid-email",
                "password123"
        );

        // Act
        Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    void shouldFailValidation_WhenPasswordIsTooShort() {
        // Arrange
        SignupRequestDTO dto = new SignupRequestDTO(
                "John Doe",
                "john@example.com",
                "pass"
        );

        // Act
        Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("at least 8 characters")));
    }

    @Test
    void shouldFailValidation_WhenMultipleFieldsAreInvalid() {
        // Arrange
        SignupRequestDTO dto = new SignupRequestDTO(
                "",
                "invalid-email",
                "123"
        );

        // Act
        Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(dto);

        // Assert
        // Au moins 3 violations, mais peut-être 4 si @NotBlank ET @Size échouent
        assertTrue(violations.size() >= 3);
    }
}