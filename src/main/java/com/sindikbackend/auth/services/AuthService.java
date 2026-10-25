package com.sindikbackend.auth.services;

import com.sindikbackend.auth.dtos.requests.AuthRequest;
import com.sindikbackend.auth.dtos.requests.RegisterRequest;
import com.sindikbackend.auth.dtos.responses.AuthResponse;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.UserRepository;
import com.sindikbackend.auth.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;



    // =====================================
    // Enregistrement
    // =====================================
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                // Public registration must never grant administrative or staff roles.
                .role(com.sindikbackend.auth.entities.Role.RESIDENT)
                .isActive(true)
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", req.getEmail());
        String token = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        refreshTokenService.create(user, refreshToken);
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .uuid(user.getUuid())
                .expiresIn(jwtUtils.getExpirationMs() / (1000 * 3600))
                .build();
    }

    // =====================================
    // Connexion
    // =====================================


    public AuthResponse login(AuthRequest req) {
        User user = findByEmail(req.getEmail());
        // ✅ expiresIn = durée en millisecondes
        long expiresInHours = jwtUtils.getExpirationMs() / (1000 * 3600);

        if (!user.isEnabled() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()
                || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Mise à jour last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        refreshTokenService.create(user, refreshToken);
        log.info("User logged in successfully: {}", req.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .uuid(user.getUuid())
                .expiresIn(expiresInHours) // en heures
                .build();
    }

    // =====================================
    // Chargement utilisateur pour Spring Security
    // =====================================
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResponse refresh(String token) {
        if (token == null || token.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        final User user;
        try {
            String email = jwtUtils.extractUsername(token);
            user = findByEmail(email);
            if (!jwtUtils.isValidRefreshToken(token, user)) {
                throw new BadCredentialsException("Invalid refresh token");
            }
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User storedUser = refreshTokenService.consume(token);
        if (!storedUser.getId().equals(user.getId())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (!user.isEnabled() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String accessToken = jwtUtils.generateToken(user);
        String rotatedRefreshToken = jwtUtils.generateRefreshToken(user);
        refreshTokenService.create(user, rotatedRefreshToken);
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(rotatedRefreshToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .uuid(user.getUuid())
                .expiresIn(jwtUtils.getExpirationMs() / (1000 * 3600))
                .build();
    }

    // LOGOUT SIMPLE
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.delete(refreshToken);
            log.info("Logout successful - refresh token deleted");
        } else {
            log.info("Logout without refresh token");
        }
    }

}
