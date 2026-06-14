package org.example.rest.security;

import org.example.rest.security.user.UserAuthority;
import org.example.rest.security.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.secret.issuer}") String issuer,
            @Value("${security.jwt.secret.access-token-minutes}") long accessTokenMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(String email, UserRole role, Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenMinutes * 60);

        List<String> authorities = role.getAuthorities()
                .stream()
                .map(UserAuthority::name)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(email)
                .claim("roles", List.of(role.name()))
                .claim("authorities", authorities)
                .claim("userId", userId)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenMinutes * 60;
    }
}