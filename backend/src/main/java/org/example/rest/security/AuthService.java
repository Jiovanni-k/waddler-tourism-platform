package org.example.rest.security;

import org.example.rest.security.user.*;
import org.example.rest.notification.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final long refreshTokenDays;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            RefreshTokenRepository refreshTokenRepository,
            EmailService emailService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            @Value("${security.jwt.refresh-token-days:7}") long refreshTokenDays
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public LoginResponse login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (user.getStatus() == UserStatus.SUSPENDED)
            throw new AuthException("Account is suspended");

        if (user.getStatus() == UserStatus.PENDING_APPROVAL)
            throw new AuthException("Your account is pending admin approval. You will receive an email once approved.");

        if (user.getPasswordHash() == null)
            throw new AuthException("This account uses Google Sign-In. Please login with Google.");

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new AuthException("Invalid credentials");

        String accessToken = jwtTokenService.generateAccessToken(
                user.getEmail(), user.getRole(), user.getId());
        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds());
    }

    @Transactional
    public LoginResponse signUp(SignUpRequest req) {
        if (req.role() == UserRole.ADMIN)
            throw new AuthException("Cannot sign up with ADMIN role");

        if (req.role() != null && req.role() != UserRole.USER && req.role() != UserRole.HOTEL_MANAGER)
            throw new AuthException("Invalid role for self-registration");

        if (userRepository.existsByEmail(req.email()))
            throw new AuthException("Email already in use");

        AppUser user = new AppUser();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setBirthDate(req.birthDate());
        user.setGender(req.gender());
        user.setRole(req.role() != null ? req.role() : UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        if (user.getRole() == UserRole.HOTEL_MANAGER) {
            user.setStatus(UserStatus.PENDING_APPROVAL);
        }

        AppUser saved = userRepository.save(user);

        if (saved.getRole() == UserRole.HOTEL_MANAGER) {
            try {
                emailService.sendHotelManagerRegistrationUnderReview(saved.getEmail(), saved.getFirstName());
            } catch (Exception e) {
                log.error("Failed to send under-review email to {}: {}", saved.getEmail(), e.getMessage());
            }
            userRepository.findAllByRole(UserRole.ADMIN).forEach(admin -> {
                try {
                    emailService.sendNewManagerNotificationToAdmin(
                            admin.getEmail(), admin.getFirstName(), saved);
                } catch (Exception e) {
                    log.error("Failed to notify admin {} of new manager: {}", admin.getEmail(), e.getMessage());
                }
            });
            return new LoginResponse(null, null, "Bearer",
                    jwtTokenService.getAccessTokenExpiresInSeconds());
        }

        try {
            emailService.sendWelcome(saved.getEmail(), saved.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", saved.getEmail(), e.getMessage());
        }

        String accessToken = jwtTokenService.generateAccessToken(
                saved.getEmail(), saved.getRole(), saved.getId());
        String refreshToken = createRefreshToken(saved);

        return new LoginResponse(accessToken, refreshToken, "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds());
    }

    @Transactional
    public LoginResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (refreshToken.isRevoked())
            throw new AuthException("Refresh token has been revoked");

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token has expired, please log in again");
        }

        AppUser user = refreshToken.getUser();

        if (user.getStatus() == UserStatus.SUSPENDED)
            throw new AuthException("Account is suspended");

        String newAccessToken = jwtTokenService.generateAccessToken(
                user.getEmail(), user.getRole(), user.getId());
        String newRefreshToken = rotateRefreshToken(refreshToken);

        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer",
                jwtTokenService.getAccessTokenExpiresInSeconds());
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword()))
            throw new AuthException("New password and confirmation do not match");

        Long userId = SecurityUtil.getCurrentUserId();
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash()))
            throw new AuthException("Current password is incorrect");

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(user -> {
            if (user.getStatus() == UserStatus.SUSPENDED) return;

            // Delete any existing OTP for this email
            passwordResetTokenRepository.deleteByEmail(req.email());

            // Generate a 6-digit OTP
            String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            Instant expiry = Instant.now().plusSeconds(10 * 60); // 10 minutes

            passwordResetTokenRepository.save(new PasswordResetToken(null, otp, req.email(), expiry, false));

            try {
                emailService.sendPasswordResetOtp(req.email(), user.getFirstName(), otp);
            } catch (Exception e) {
                log.error("Failed to send OTP email to {}: {}", req.email(), e.getMessage());
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword()))
            throw new AuthException("Passwords do not match");

        PasswordResetToken token = passwordResetTokenRepository
                .findByCodeAndEmail(req.otp(), req.email())
                .orElseThrow(() -> new AuthException("Invalid OTP"));

        if (token.isExpired())
            throw new AuthException("OTP has expired. Please request a new one.");

        if (token.isUsed())
            throw new AuthException("OTP has already been used.");

        AppUser user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new AuthException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        refreshTokenRepository.deleteByUser(user);
    }

    private String createRefreshToken(AppUser user) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60);
        refreshTokenRepository.save(new RefreshToken(token, user, expiry));
        return token;
    }

    private String rotateRefreshToken(RefreshToken old) {
        refreshTokenRepository.delete(old);
        return createRefreshToken(old.getUser());
    }

    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$!";
        String all = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        for (int i = 0; i < 6; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }

        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }

    public UserProfileDto.Response getProfile(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileDto.Response updateProfile(Long userId, UserProfileDto.UpdateRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getNationality() != null) user.setNationality(request.getNationality());
        if (request.getMaritalStatus() != null) user.setMaritalStatus(request.getMaritalStatus());
        if (request.getFamilyMembersCount() != null) user.setFamilyMembersCount(request.getFamilyMembersCount());
        if (request.getHasKids() != null) user.setHasKids(request.getHasKids());

        return toProfileResponse(userRepository.save(user));
    }

    private UserProfileDto.Response toProfileResponse(AppUser user) {
        return UserProfileDto.Response.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .phone(user.getPhone())
                .nationality(user.getNationality())
                .maritalStatus(user.getMaritalStatus())
                .familyMembersCount(user.getFamilyMembersCount())
                .hasKids(user.getHasKids())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public void approveManager(Long userId) {
        AppUser manager = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (manager.getRole() != UserRole.HOTEL_MANAGER)
            throw new AuthException("User is not a hotel manager");

        if (manager.getStatus() != UserStatus.PENDING_APPROVAL)
            throw new AuthException("Manager is not pending approval");

        manager.setStatus(UserStatus.ACTIVE);
        userRepository.save(manager);

        try {
            emailService.sendHotelManagerApproved(manager.getEmail(), manager.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send approval email to {}: {}", manager.getEmail(), e.getMessage());
        }
    }

    @Transactional
    public void rejectManager(Long userId, String reason) {
        AppUser manager = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (manager.getRole() != UserRole.HOTEL_MANAGER)
            throw new AuthException("User is not a hotel manager");

        if (manager.getStatus() != UserStatus.PENDING_APPROVAL)
            throw new AuthException("Manager is not pending approval");

        manager.setStatus(UserStatus.SUSPENDED);
        userRepository.save(manager);

        try {
            emailService.sendHotelManagerRejected(manager.getEmail(), manager.getFirstName(), reason);
        } catch (Exception e) {
            log.error("Failed to send rejection email to {}: {}", manager.getEmail(), e.getMessage());
        }
    }

    public List<UserProfileDto.Response> getPendingManagers() {
        return userRepository.findAllByRoleAndStatus(UserRole.HOTEL_MANAGER, UserStatus.PENDING_APPROVAL)
                .stream()
                .map(this::toProfileResponse)
                .toList();
    }
}