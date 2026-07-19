package com.smartcity.nav.service;

import com.smartcity.nav.dto.*;
import com.smartcity.nav.entity.RefreshToken;
import com.smartcity.nav.entity.Role;
import com.smartcity.nav.entity.User;
import com.smartcity.nav.exception.BadRequestException;
import com.smartcity.nav.exception.ConflictException;
import com.smartcity.nav.exception.ResourceNotFoundException;
import com.smartcity.nav.repository.RefreshTokenRepository;
import com.smartcity.nav.repository.RoleRepository;
import com.smartcity.nav.repository.UserRepository;
import com.smartcity.nav.security.CustomUserDetailsService;
import com.smartcity.nav.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business logic for the authentication flow: register, login, and
 * refresh-token exchange. Controllers stay thin and delegate here.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    private static final long REFRESH_TOKEN_EXPIRATION_MS = 604_800_000L; // 7 days

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }

        Role citizenRole = roleRepository.findByName("CITIZEN")
                .orElseThrow(() -> new ResourceNotFoundException("Default role CITIZEN not seeded in DB"));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(citizenRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Delegates credential checking to Spring Security's DaoAuthenticationProvider,
        // which uses CustomUserDetailsService + the configured PasswordEncoder.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (Boolean.TRUE.equals(storedToken.getRevoked()) || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expired or revoked - please log in again");
        }

        User user = storedToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(storedToken.getToken()) // reuse existing refresh token
                .tokenType("Bearer")
                .userId(user.getId())
                .role(user.getRole().getName())
                .build();
    }

    private AuthResponse issueTokens(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshTokenValue = jwtUtil.generateRefreshToken(userDetails);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_MS / 1000))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .userId(user.getId())
                .role(user.getRole().getName())
                .build();
    }
}
