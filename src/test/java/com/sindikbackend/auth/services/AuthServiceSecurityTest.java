package com.sindikbackend.auth.services;

import com.sindikbackend.auth.dtos.requests.RegisterRequest;
import com.sindikbackend.auth.dtos.requests.AuthRequest;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.UserRepository;
import com.sindikbackend.auth.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceSecurityTest {
    @Test
    void publicRegistrationAlwaysCreatesResidentEvenWhenPrivilegedRoleWasRequested() {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        JwtUtils jwt = mock(JwtUtils.class);
        RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
        when(users.existsByEmail("new@example.com")).thenReturn(false);
        when(passwords.encode("password123")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwt.generateToken(any(User.class))).thenReturn("access");
        when(jwt.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthService service = new AuthService(users, passwords, jwt, refreshTokens);
        RegisterRequest request = RegisterRequest.builder()
                .fullName("New Resident")
                .email("new@example.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        var response = service.register(request);

        verify(users).save(argThat(user -> user.getRole() == Role.RESIDENT));
        verify(refreshTokens).create(argThat(user -> user.getRole() == Role.RESIDENT), eq("refresh"));
        assertEquals("RESIDENT", response.getRole());
    }

    @Test
    void loginRejectsDisabledAccountsWithGenericAuthenticationError() {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        User disabled = User.builder().email("disabled@example.com").password("encoded")
                .role(Role.RESIDENT).isActive(false).isAccountNonLocked(true)
                .isAccountNonExpired(true).isCredentialsNonExpired(true).build();
        when(users.findByEmail("disabled@example.com")).thenReturn(java.util.Optional.of(disabled));
        AuthService service = new AuthService(users, passwords, mock(JwtUtils.class), mock(RefreshTokenService.class));

        assertThrows(BadCredentialsException.class, () -> service.login(
                AuthRequest.builder().email("disabled@example.com").password("password123").build()));
        verifyNoInteractions(passwords);
    }

    @Test
    void refreshConsumesOldTokenAndPersistsRotatedToken() {
        UserRepository users = mock(UserRepository.class);
        User user = User.builder().id(11L).email("active@example.com").role(Role.RESIDENT)
                .isActive(true).isAccountNonLocked(true).isAccountNonExpired(true)
                .isCredentialsNonExpired(true).build();
        when(users.findByEmail("active@example.com")).thenReturn(java.util.Optional.of(user));
        JwtUtils jwt = mock(JwtUtils.class);
        when(jwt.extractUsername("old-refresh")).thenReturn("active@example.com");
        when(jwt.isValidRefreshToken("old-refresh", user)).thenReturn(true);
        when(jwt.generateToken(user)).thenReturn("new-access");
        when(jwt.generateRefreshToken(user)).thenReturn("new-refresh");
        when(jwt.getExpirationMs()).thenReturn(86_400_000L);
        RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
        when(refreshTokens.consume("old-refresh")).thenReturn(user);
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, refreshTokens);

        var response = service.refresh("old-refresh");

        assertEquals("new-access", response.getToken());
        assertEquals("new-refresh", response.getRefreshToken());
        verify(refreshTokens).consume("old-refresh");
        verify(refreshTokens).create(user, "new-refresh");
    }
}
