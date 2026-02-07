package com.sindikbackend.auth.services;

import com.sindikbackend.auth.entities.User;
import com.sindikbackend.auth.repositories.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class  UserService implements UserDetailsService {

    private final UserRepository repo;
    public User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );
    }

    // 🔐 MÉTHODE OBLIGATOIRE POUR SPRING SECURITY
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email)
            throws UsernameNotFoundException {
        return findByEmail(email);
    }
}
