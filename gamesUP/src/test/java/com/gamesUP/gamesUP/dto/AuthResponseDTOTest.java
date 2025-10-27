package com.gamesUP.gamesUP.dto.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeToJson() throws Exception {
        // Arrange
        AuthResponseDTO dto = new AuthResponseDTO(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER",
                "dummy-jwt-token"
        );

        // Act
        String json = objectMapper.writeValueAsString(dto);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\"userId\":1"));
        assertTrue(json.contains("\"fullName\":\"John Doe\""));
        assertTrue(json.contains("\"email\":\"john@example.com\""));
        assertTrue(json.contains("\"role\":\"CUSTOMER\""));
        assertTrue(json.contains("\"token\":\"dummy-jwt-token\""));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Arrange
        String json = "{\"userId\":1,\"fullName\":\"John Doe\",\"email\":\"john@example.com\",\"role\":\"CUSTOMER\",\"token\":\"dummy-jwt-token\"}";

        // Act
        AuthResponseDTO dto = objectMapper.readValue(json, AuthResponseDTO.class);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getUserId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("CUSTOMER", dto.getRole());
        assertEquals("dummy-jwt-token", dto.getToken());
    }
}