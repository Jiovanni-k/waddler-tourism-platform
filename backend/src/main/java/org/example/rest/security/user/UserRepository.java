package org.example.rest.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    java.util.List<AppUser> findAllByRole(UserRole role);

    java.util.List<AppUser> findAllByStatus(UserStatus status);

    java.util.List<AppUser> findAllByRoleAndStatus(UserRole role, UserStatus status);
}