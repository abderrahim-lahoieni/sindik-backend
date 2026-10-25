package com.sindikbackend.auth.services;

import com.sindikbackend.auth.dtos.requests.UserUpdateRequest;
import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.RefreshTokenRepository;
import com.sindikbackend.auth.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserCrudServiceSecurityTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserCrudService service = new UserCrudService(
            users, mock(PasswordEncoder.class), mock(RefreshTokenRepository.class));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userCannotPromoteSelfOrChangeProtectedAccountStates() {
        authenticateAs("resident@example.com", "ROLE_RESIDENT");
        User user = resident();
        when(users.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        UserUpdateRequest[] attempts = {
                request().role(Role.ADMIN).build(),
                request().isActive(false).build(),
                request().isAccountNonLocked(false).build(),
                request().isAccountNonExpired(false).build(),
                request().isCredentialsNonExpired(false).build()
        };

        for (UserUpdateRequest attempt : attempts) {
            assertThrows(AccessDeniedException.class, () -> service.updateUser(user.getUuid(), attempt));
        }

        assertEquals(Role.RESIDENT, user.getRole());
        assertEquals(true, user.getIsActive());
        assertEquals(true, user.getIsAccountNonLocked());
        assertEquals(true, user.getIsAccountNonExpired());
        assertEquals(true, user.getIsCredentialsNonExpired());
        verify(users, never()).save(any(User.class));
    }

    @Test
    void userCanStillUpdateOwnProfile() {
        authenticateAs("resident@example.com", "ROLE_RESIDENT");
        User user = resident();
        when(users.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUser(user.getUuid(), UserUpdateRequest.builder()
                .fullName("Updated Resident").email("updated@example.com").build());

        assertEquals("Updated Resident", user.getFullName());
        assertEquals("updated@example.com", user.getEmail());
        assertEquals(Role.RESIDENT, user.getRole());
    }

    @Test
    void adminCanUpdateAdministrativeFields() {
        authenticateAs("admin@example.com", "ROLE_ADMIN");
        User user = resident();
        when(users.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUser(user.getUuid(), request().role(Role.ADMIN).isActive(false)
                .isAccountNonLocked(false).isAccountNonExpired(false)
                .isCredentialsNonExpired(false).build());

        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(false, user.getIsActive());
        assertEquals(false, user.getIsAccountNonLocked());
        assertEquals(false, user.getIsAccountNonExpired());
        assertEquals(false, user.getIsCredentialsNonExpired());
    }

    private static UserUpdateRequest.UserUpdateRequestBuilder request() {
        return UserUpdateRequest.builder().fullName("Resident").email("resident@example.com");
    }

    private static User resident() {
        return User.builder().uuid(UUID.randomUUID()).fullName("Resident").email("resident@example.com")
                .password("encoded").role(Role.RESIDENT).isActive(true).isAccountNonLocked(true)
                .isAccountNonExpired(true).isCredentialsNonExpired(true).build();
    }

    private static void authenticateAs(String name, String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                name, "", java.util.List.of(new SimpleGrantedAuthority(authority))));
    }
}
