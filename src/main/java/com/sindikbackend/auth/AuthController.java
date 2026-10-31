package com.sindikbackend.auth;

import com.sindikbackend.auth.dtos.requests.AuthRequest;
import com.sindikbackend.auth.dtos.requests.RegisterRequest;
import com.sindikbackend.auth.dtos.responses.AuthResponse;
import com.sindikbackend.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.refresh(body == null ? null : body.get("refreshToken")));
    }

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        return ResponseEntity.ok("User authenticated successfully");
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestBody(required = false) Map<String, String> body) {

        String refreshToken = null;

        // 1. Essayer de récupérer du body
        if (body != null && body.containsKey("refreshToken")) {
            refreshToken = body.get("refreshToken");
        }

        // 2. Essayer de récupérer du header Authorization
        if (refreshToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
        }

        authService.logout(refreshToken);

        // ✅ Toujours retourner 200 OK même si token non trouvé
        return ResponseEntity.ok()
                .body(Map.of("message", "Logout successful"));
    }

}
