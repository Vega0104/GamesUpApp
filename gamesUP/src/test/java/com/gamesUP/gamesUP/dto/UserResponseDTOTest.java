package com.gamesUP.gamesUP.dto.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateUserResponseDTO() {
        // Arrange & Act
        UserResponseDTO dto = new UserResponseDTO(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER",
                "2025-01-15T10:30:00"
        );

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("CUSTOMER", dto.getRole());
        assertEquals("2025-01-15T10:30:00", dto.getCreatedAt());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Arrange
        UserResponseDTO dto = new UserResponseDTO(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER",
                "2025-01-15T10:30:00"
        );

        // Act
        String json = objectMapper.writeValueAsString(dto);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"fullName\":\"John Doe\""));
        assertTrue(json.contains("\"email\":\"john@example.com\""));
        assertTrue(json.contains("\"role\":\"CUSTOMER\""));
        assertTrue(json.contains("\"createdAt\":\"2025-01-15T10:30:00\""));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Arrange
        String json = "{\"id\":1,\"fullName\":\"John Doe\",\"email\":\"john@example.com\",\"role\":\"CUSTOMER\",\"createdAt\":\"2025-01-15T10:30:00\"}";

        // Act
        UserResponseDTO dto = objectMapper.readValue(json, UserResponseDTO.class);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("CUSTOMER", dto.getRole());
        assertEquals("2025-01-15T10:30:00", dto.getCreatedAt());
    }

    @Test
    void shouldNotContainPassword() {
        // Arrange
        UserResponseDTO dto = new UserResponseDTO(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER",
                "2025-01-15T10:30:00"
        );

        // Act
        String json = null;
        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            fail("Serialization should not fail");
        }

        // Assert
        assertFalse(json.contains("password"));
        assertFalse(json.contains("passwordHash"));
    }
}