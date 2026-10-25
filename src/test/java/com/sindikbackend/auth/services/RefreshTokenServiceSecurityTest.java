package com.sindikbackend.auth.services;

import com.sindikbackend.auth.entities.RefreshToken;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceSecurityTest {
    @Test
    void consumeDeletesStoredTokenAndRejectsReuse() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        User user = User.builder().id(7L).email("resident@example.com").role(Role.RESIDENT).build();
        RefreshToken stored = RefreshToken.builder().token("signed-refresh-token")
                .expiryDate(LocalDateTime.now().plusDays(1)).user(user).build();
        when(repository.findByTokenForUpdate("signed-refresh-token"))
                .thenReturn(Optional.of(stored), Optional.empty());
        RefreshTokenService service = new RefreshTokenService(repository);

        assertSame(user, service.consume("signed-refresh-token"));
        verify(repository).delete(stored);
        assertThrows(BadCredentialsException.class, () -> service.consume("signed-refresh-token"));
    }

    @Test
    void consumeRejectsAndDeletesExpiredStoredToken() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshToken stored = RefreshToken.builder().token("expired")
                .expiryDate(LocalDateTime.now().minusSeconds(1))
                .user(User.builder().id(7L).email("resident@example.com").role(Role.RESIDENT).build())
                .build();
        when(repository.findByTokenForUpdate("expired")).thenReturn(Optional.of(stored));
        RefreshTokenService service = new RefreshTokenService(repository);

        assertThrows(BadCredentialsException.class, () -> service.consume("expired"));
        verify(repository).delete(stored);
    }
}
