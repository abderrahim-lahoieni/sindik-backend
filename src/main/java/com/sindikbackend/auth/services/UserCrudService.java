package com.sindikbackend.auth.services;

import com.sindikbackend.auth.dtos.requests.UserCreateRequest;
import com.sindikbackend.auth.dtos.requests.UserUpdateRequest;
import com.sindikbackend.auth.dtos.responses.UserResponse;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.auth.exceptions.BadRequestException;
import com.sindikbackend.auth.repositories.RefreshTokenRepository;
import com.sindikbackend.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCrudService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // ✅ AJOUTE CETTE DEPENDANCE


    // ========================================
    // MAPPER METHODS
    // ========================================

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isAccountNonLocked(user.getIsAccountNonLocked())
                .isAccountNonExpired(user.getIsAccountNonExpired())
                .isCredentialsNonExpired(user.getIsCredentialsNonExpired())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        log.info("Finding all users");
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findUserByUuid(UUID uuid) {
        log.info("Finding user by uuid: {}", uuid);
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + uuid));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findUsersByRole(String role) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            log.info("Finding users by role: {}", roleEnum);
            return userRepository.findByRoleActive(roleEnum).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + role);
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findActiveUsers() {
        log.info("Finding active users");
        return userRepository.findAllActiveUsers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findLockedUsers() {
        log.info("Finding locked users");
        return userRepository.findLockedUsers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findNeverLoggedInUsers() {
        log.info("Finding never logged in users");
        return userRepository.findNeverLoggedInUsers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        log.info("Searching users with keyword: {}", keyword);
        return userRepository.searchByFullName(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<UserResponse> findUsers(String role, Boolean active) {

        Role roleEnum = null;

        // Convert role si présent
        if (role != null && !role.isBlank()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + role);
            }
        }

        List<User> users = userRepository.findByFilters(roleEnum, active);

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ========================================
    // CREATE OPERATION
    // ========================================

    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with uuid: {}", savedUser.getUuid());

        return mapToResponse(savedUser);
    }

    // ========================================
    // UPDATE OPERATION
    // ========================================

    public UserResponse updateUser(UUID uuid, UserUpdateRequest request) {
        log.info("Updating user with uuid: {}", uuid);

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + uuid));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Update fields
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getIsAccountNonLocked() != null) {
            user.setIsAccountNonLocked(request.getIsAccountNonLocked());
        }

        if (request.getIsAccountNonExpired() != null) {
            user.setIsAccountNonExpired(request.getIsAccountNonExpired());
        }

        if (request.getIsCredentialsNonExpired() != null) {
            user.setIsCredentialsNonExpired(request.getIsCredentialsNonExpired());
        }

        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with uuid: {}", updatedUser.getUuid());

        return mapToResponse(updatedUser);
    }

    // ========================================
    // DELETE OPERATION
    // ========================================

// ========================================
// DELETE OPERATION - CORRIGÉE ✅
// ========================================

    @Transactional
    public void deleteUser(UUID uuid) {
        log.info("========== DELETE USER ==========");
        log.info("UUID reçu: {}", uuid);

        // 1️⃣ Récupérer l'utilisateur
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + uuid));

        log.info("👤 Utilisateur trouvé: {} - {}", user.getEmail(), user.getFullName());

        // 2️⃣ SUPPRIMER D'ABORD TOUS LES REFRESH TOKENS (CRUCIAL !)
        log.info("🔄 Suppression des refresh tokens pour l'utilisateur: {}", user.getEmail());
        int deletedTokens = refreshTokenRepository.deleteAllByUser(user);
        log.info("✅ {} refresh token(s) supprimés", deletedTokens);

        // 3️⃣ MAINTENANT ON PEUT SUPPRIMER L'UTILISATEUR
        userRepository.delete(user);
        log.info("✅ Utilisateur supprimé avec succès: {}", uuid);

        log.info("=================================");
    }
    // ========================================
    // STATUS MANAGEMENT
    // ========================================

    public UserResponse toggleUserStatus(UUID uuid) {
        log.info("Toggling status for user: {}", uuid);

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + uuid));

        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);

        log.info("User status toggled to: {} for uuid: {}", user.getIsActive(), uuid);

        return mapToResponse(updatedUser);
    }

    public UserResponse toggleUserLock(UUID uuid) {
        log.info("Toggling lock for user: {}", uuid);

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + uuid));

        user.setIsAccountNonLocked(!user.getIsAccountNonLocked());
        User updatedUser = userRepository.save(user);

        log.info("User lock toggled to: {} for uuid: {}", user.getIsAccountNonLocked(), uuid);

        return mapToResponse(updatedUser);
    }
}