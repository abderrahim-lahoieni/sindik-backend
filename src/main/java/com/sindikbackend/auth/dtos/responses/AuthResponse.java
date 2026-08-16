package com.sindikbackend.auth.dtos.responses;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private String role;
    private Long expiresIn;
    private UUID uuid;
}
