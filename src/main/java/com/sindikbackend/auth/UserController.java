package com.sindikbackend.auth;

import com.sindikbackend.auth.dtos.requests.UserCreateRequest;
import com.sindikbackend.auth.dtos.requests.UserUpdateRequest;
import com.sindikbackend.auth.dtos.responses.UserResponse;
import com.sindikbackend.auth.services.UserCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserCrudService userCrudService;

    // ========================================
    // CRUD OPERATIONS
    // ========================================

//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<UserResponse>> getAllUsers() {
//        log.info("REST request to get all users");
//        return ResponseEntity.ok(userCrudService.findAllUsers());
//    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#uuid)")
    public ResponseEntity<UserResponse> getUserByUuid(@PathVariable UUID uuid) {
        log.info("REST request to get user by uuid: {}", uuid);
        return ResponseEntity.ok(userCrudService.findUserByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("REST request to create user: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userCrudService.createUser(request));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#uuid)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID uuid,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update user: {}", uuid);
        return ResponseEntity.ok(userCrudService.updateUser(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        log.info("REST request to delete user: {}", uuid);
        userCrudService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // STATUS MANAGEMENT
    // ========================================

    @PatchMapping("/{uuid}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable UUID uuid) {
        log.info("REST request to toggle status for user: {}", uuid);
        return ResponseEntity.ok(userCrudService.toggleUserStatus(uuid));
    }
    // ========================================
    // SEARCH & FILTERS
    // ========================================

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        log.info("REST request to search users with keyword: {}", keyword);
        return ResponseEntity.ok(userCrudService.searchUsers(keyword));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        log.info("REST request to get users by role: {}", role);
        return ResponseEntity.ok(userCrudService.findUsersByRole(role));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        log.info("REST request to get active users");
        return ResponseEntity.ok(userCrudService.findActiveUsers());
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("REST request to get users | role: {} | active: {}", role, active);
        return ResponseEntity.ok(userCrudService.findUsers(role, active));
    }



}