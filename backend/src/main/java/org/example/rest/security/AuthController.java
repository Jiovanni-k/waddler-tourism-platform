package org.example.rest.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.security.user.UserProfileDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody @Valid GoogleTokenRequest req) {
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(req.idToken()));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.email(), req.password()));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signUp(@RequestBody @Valid SignUpRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refreshToken(req.refreshToken()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok("If that email is registered, an OTP has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok("Password reset successfully. You can now log in.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(@RequestBody @Valid RefreshTokenRequest req) {
        authService.revokeRefreshToken(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        authService.changePassword(req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending-managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDto.Response>> getPendingManagers() {
        return ResponseEntity.ok(authService.getPendingManagers());
    }

    @PatchMapping("/managers/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveManager(@PathVariable Long userId) {
        authService.approveManager(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/managers/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectManager(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        authService.rejectManager(userId, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto.Response> getMe() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto.Response> updateMe(
            @RequestBody UserProfileDto.UpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }
}