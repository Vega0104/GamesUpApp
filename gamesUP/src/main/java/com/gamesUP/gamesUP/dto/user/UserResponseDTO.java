package com.gamesUP.gamesUP.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String createdAt; // Format ISO-8601
}