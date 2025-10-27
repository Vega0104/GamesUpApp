package com.gamesUP.gamesUP.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String token; // later
}