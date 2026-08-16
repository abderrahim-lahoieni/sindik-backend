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
                .role(req.getRole())
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
                .build();
    }

    // =====================================
    // Connexion
    // =====================================


    public AuthResponse login(AuthRequest req) {
        User user = findByEmail(req.getEmail());
        // ✅ expiresIn = durée en millisecondes
        long expiresInHours = jwtUtils.getExpirationMs() / (1000 * 3600);

        if (!user.isEnabled()) {
            throw new BadCredentialsException("User account is disabled");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
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

    // LOGOUT SIMPLE
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                refreshTokenService.delete(refreshToken);
                log.info("Logout successful - refresh token deleted");
            } catch (Exception e) {
                log.error("Error deleting refresh token: {}", e.getMessage());
                // Ne pas bloquer le logout
            }
        } else {
            log.info("Logout without refresh token");
        }
    }

}
