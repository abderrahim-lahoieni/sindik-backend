package com.sindikbackend.auth.services;

import com.sindikbackend.auth.entities.RefreshToken;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // durée refresh token : 7 jours
    private static final long REFRESH_TOKEN_DAYS = 1;

    public void create(User user, String token) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .uuid(UUID.randomUUID())
                .build();

        refreshTokenRepository.save(refreshToken);
    }


    public void delete(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

}
