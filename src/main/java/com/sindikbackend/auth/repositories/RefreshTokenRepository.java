package com.sindikbackend.auth.repositories;

import com.sindikbackend.auth.entities.RefreshToken;
import com.sindikbackend.auth.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.token = :value")
    Optional<RefreshToken> findByTokenForUpdate(@Param("value") String token);

    void deleteByToken(String token);

    @Modifying
    @Transactional
    int deleteAllByUser(User user);  // ✅ Cette méthode doit exister
}
