package com.sindikbackend.auth;

import com.sindikbackend.auth.dtos.requests.AuthRequest;
import com.sindikbackend.auth.dtos.requests.RegisterRequest;
import com.sindikbackend.auth.dtos.responses.AuthResponse;
import com.sindikbackend.auth.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // allow only your frontend
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}
