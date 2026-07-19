package com.smartcity.nav.security;

import com.smartcity.nav.entity.User;
import com.smartcity.nav.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads User entities from the DB and adapts them to Spring Security's
 * UserDetails contract for authentication.
 *
 * We use email as the "username" for Spring Security purposes.
 * Role is prefixed with ROLE_ so it works with Spring's hasRole("ADMIN")
 * checks (which internally look for a "ROLE_ADMIN" authority).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())))
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                .build();
    }
}
