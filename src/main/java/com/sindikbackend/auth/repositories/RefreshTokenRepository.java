package com.sindikbackend.auth.repositories;

import com.sindikbackend.auth.entities.RefreshToken;
import com.sindikbackend.auth.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    @Modifying
    @Transactional
    int deleteAllByUser(User user);  // ✅ Cette méthode doit exister
}
