package org.example.rest.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.jwt.secret}")
    private String base64Secret;

    private final AuthEntryPointJwt authEntryPointJwt;

    public SecurityConfig(AuthEntryPointJwt authEntryPointJwt) {
        this.authEntryPointJwt = authEntryPointJwt;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPointJwt))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/google").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{id}/distance").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{id}/amenities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/rooms").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/rooms/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/rooms/{id}/amenities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/cancellation-policies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/cancellation-policies/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/amenities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/contact").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/reviews/summary").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/{eventId}/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/{eventId}/reviews/summary").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/rooms/{roomId}/pricing-rules").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hotels/{hotelId}/rooms/{roomId}/pricing-rules/{id}").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                        .authenticationEntryPoint(authEntryPointJwt)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] decoded = Base64.getDecoder().decode(base64Secret);
        return new SecretKeySpec(decoded, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (Objects.nonNull(roles)) {
                roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .forEach(grantedAuthorities::add);
            }

            List<String> authorities = jwt.getClaimAsStringList("authorities");
            if (Objects.nonNull(authorities)) {
                authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .forEach(grantedAuthorities::add);
            }

            return grantedAuthorities;
        });
        return conv;
    }
}