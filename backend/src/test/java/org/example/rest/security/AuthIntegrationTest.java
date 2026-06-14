package org.example.rest.security;

import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserGender;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserRole;
import org.example.rest.security.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testEmail = "inttest@example.com";
    private final String testPassword = "TestPassword@123";

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
    }

    @Test
    void testUserCreation_Success() {
        AppUser user = new AppUser();
        user.setEmail(testEmail);
        user.setPasswordHash(passwordEncoder.encode(testPassword));
        user.setFirstName("Integration");
        user.setLastName("Test");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setGender(UserGender.MALE);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        AppUser savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals(testEmail, savedUser.getEmail());
        assertTrue(passwordEncoder.matches(testPassword, savedUser.getPasswordHash()));
    }

    @Test
    void testUserFindByEmail_Success() {
        AppUser user = new AppUser();
        user.setEmail(testEmail);
        user.setPasswordHash(passwordEncoder.encode(testPassword));
        user.setFirstName("Find");
        user.setLastName("Test");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setGender(UserGender.FEMALE);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        var foundUser = userRepository.findByEmail(testEmail);

        assertTrue(foundUser.isPresent());
        assertEquals(testEmail, foundUser.get().getEmail());
        assertEquals("Find", foundUser.get().getFirstName());
    }
}