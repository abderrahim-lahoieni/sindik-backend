package com.sindikbackend.auth.repositories;

import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========================================
    // Méthodes de Base (CRUD)
    // ========================================

    /**
     * Trouver un utilisateur par email
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifier si un email existe (CELLE-CI EST MANQUANTE)
     */
    boolean existsByEmail(String email);

    /**
     * Trouver un utilisateur par UUID
     */
    Optional<User> findByUuid(UUID uuid);

    /**
     * Vérifier si un UUID existe
     */
    boolean existsByUuid(UUID uuid);

    // ========================================
    // Requêtes Personnalisées
    // ========================================

    /**
     * Trouver tous les utilisateurs actifs
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findAllActiveUsers();

    /**
     * Trouver les utilisateurs par rôle
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findByRoleActive(@Param("role") com.sindikbackend.auth.entities.Role role);

    /**
     * Compter les utilisateurs par rôle
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") com.sindikbackend.auth.entities.Role role);

    /**
     * Trouver les utilisateurs verrouillés
     */
    @Query("SELECT u FROM User u WHERE u.isAccountNonLocked = false")
    List<User> findLockedUsers();

    /**
     * Trouver les utilisateurs inactifs
     */
    @Query("SELECT u FROM User u WHERE u.isActive = false")
    List<User> findInactiveUsers();

    /**
     * Chercher par nom complet (partial match)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByFullName(@Param("name") String name);

    /**
     * Trouver les utilisateurs avec dernier login null (jamais connectés)
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL ORDER BY u.createdAt DESC")
    List<User> findNeverLoggedInUsers();

    /**
     * Vérifier si un utilisateur est actif et déverrouillé
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
            "WHERE u.email = :email AND u.isActive = true AND u.isAccountNonLocked = true")
    boolean isUserActiveAndUnlocked(@Param("email") String email);


    //pour recuperer tt les utilisateues selon le role et selon le statut
    @Query("""
SELECT u FROM User u
WHERE (:role IS NULL OR u.role = :role)
AND (:active IS NULL OR u.isActive = :active)
ORDER BY u.createdAt DESC
""")
    List<User> findByFilters(
            @Param("role") Role role,
            @Param("active") Boolean active
    );
}
