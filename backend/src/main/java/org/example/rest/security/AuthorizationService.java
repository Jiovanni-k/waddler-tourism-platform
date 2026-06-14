package org.example.rest.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthorizationService {

    public boolean isSelf(Long userId, Authentication authentication) {
        Long currentId = extractUserId(authentication);
        return currentId != null && currentId.equals(userId);
    }

    public boolean isSelfOrAdmin(Long userId, Authentication authentication) {
        return isSelf(userId, authentication) || hasRole(authentication, "ADMIN");
    }

    public boolean isSelfOrHasRole(Long userId, Authentication authentication, String role) {
        return isSelf(userId, authentication) || hasRole(authentication, role);
    }

    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ADMIN");
    }

    public boolean isHotelManager(Authentication authentication) {
        return hasRole(authentication, "HOTEL_MANAGER");
    }

    public boolean isUser(Authentication authentication) {
        return hasRole(authentication, "USER");
    }

    public boolean isManagerOrAdmin(Authentication authentication) {
        return isHotelManager(authentication) || isAdmin(authentication);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) return null;
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) return null;
        return jwt.getClaim("userId");
    }
}