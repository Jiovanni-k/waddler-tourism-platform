package org.example.rest.security;

import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())
        );

        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPasswordHash())
                .authorities(authorities)
                .accountLocked(appUser.getStatus() == UserStatus.SUSPENDED)
                .disabled(appUser.getStatus() == UserStatus.SUSPENDED)
                .build();
    }
}
