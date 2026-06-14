package org.example.rest.security;

import org.example.rest.security.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(AppUser user);

    void deleteByToken(String token);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            "DELETE FROM RefreshToken t WHERE t.user = :user AND t.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredByUser(@org.springframework.data.repository.query.Param("user") org.example.rest.security.user.AppUser user);
}