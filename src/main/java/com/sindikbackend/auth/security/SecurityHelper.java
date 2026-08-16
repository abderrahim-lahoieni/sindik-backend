package com.sindikbackend.auth.security;// security/SecurityHelper.java

import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found");
    }

    public UUID getCurrentUserUuid() {
        return getCurrentUser().getUuid();
    }

    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    public boolean isAdmin() {
        return getCurrentUserRole() == Role.ADMIN;
    }

    public boolean isSyndic() {
        return getCurrentUserRole() == Role.SYNDIC;
    }

    public boolean isResident() {
        return getCurrentUserRole() == Role.RESIDENT;
    }

    public boolean isCurrentUser(UUID userUuid) {
        return getCurrentUserUuid().equals(userUuid);
    }
}