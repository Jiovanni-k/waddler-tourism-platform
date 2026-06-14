package org.example.rest.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.security.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final String googleClientId;
    private final long refreshTokenDays;

    public GoogleAuthService(
            UserRepository userRepository,
            JwtTokenService jwtTokenService,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${google.client-id}") String googleClientId,
            @Value("${security.jwt.refresh-token-days:7}") long refreshTokenDays
    ) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.googleClientId = googleClientId;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public LoginResponse loginWithGoogle(String idToken) {
        // 1. Verify the token with Google
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

        String googleId = payload.getSubject();       // unique Google user ID
        String email    = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName  = (String) payload.get("family_name");

        if (firstName == null) firstName = "Google";
        if (lastName  == null) lastName  = "User";

        // 2. Find or create the user
        AppUser user = findOrCreateUser(googleId, email, firstName, lastName);

        // 3. Check account status
        if (user.getStatus() == UserStatus.SUSPENDED)
            throw new AuthException("Account is suspended");

        if (user.getStatus() == UserStatus.PENDING_APPROVAL)
            throw new AuthException("Your account is pending admin approval");

        // 4. Generate our own JWT (same as normal login)
        String accessToken  = jwtTokenService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds());
    }

    private AppUser findOrCreateUser(String googleId, String email, String firstName, String lastName) {
        // Case 1: user already logged in with Google before
        Optional<AppUser> byGoogleId = userRepository.findByGoogleId(googleId);
        if (byGoogleId.isPresent()) {
            return byGoogleId.get();
        }

        // Case 2: user registered with email/password before — link Google to their account
        Optional<AppUser> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            AppUser existing = byEmail.get();
            existing.setGoogleId(googleId);   // link their Google account
            return userRepository.save(existing);
        }

        // Case 3: brand new user — create account automatically
        AppUser newUser = new AppUser();
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setPasswordHash(null);        // no password for Google users
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setBirthDate(LocalDate.of(2000, 1, 1)); // placeholder — can update profile later
        newUser.setGender(UserGender.MALE);
        newUser.setRole(UserRole.USER);
        newUser.setStatus(UserStatus.ACTIVE);

        return userRepository.save(newUser);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new AuthException("Invalid Google token");
            }

            return idToken.getPayload();

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new AuthException("Google token verification failed");
        }
    }

    private String createRefreshToken(AppUser user) {
        String token = UUID.randomUUID().toString();
        java.time.Instant expiry = java.time.Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60);
        refreshTokenRepository.save(new RefreshToken(token, user, expiry));
        return token;
    }
}