package com.sindikbackend.auth.services;

import com.sindikbackend.auth.dtos.requests.AuthRequest;
import com.sindikbackend.auth.dtos.requests.RegisterRequest;
import com.sindikbackend.auth.dtos.responses.AuthResponse;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.UserRepository;
import com.sindikbackend.auth.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserRepository repo;


    public AuthResponse register(RegisterRequest req) {

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();

        repo.save(user);

        String token = jwtUtils.generateToken(user);
        String username = jwtUtils.extractUsername(token);

        return new AuthResponse(token,username);
    }

    public AuthResponse login(AuthRequest req) {

        User user = findByEmail(req.getEmail());

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user);
        String username = jwtUtils.extractUsername(token);

        return new AuthResponse(token,username);
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );
    }
}
