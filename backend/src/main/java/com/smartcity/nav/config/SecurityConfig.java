package com.smartcity.nav.config;

import com.smartcity.nav.security.AuthEntryPoint;
import com.smartcity.nav.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Central Spring Security configuration.
 *
 * - Stateless sessions (JWT only, no server-side session state)
 * - Public: /api/auth/**  (register, login, refresh)
 * - Public read: GET /api/locations/**, /api/route/**, /api/nearby/**, /api/search/**
 *   (citizens can browse/plan routes without logging in; write ops require auth)
 * - Everything under /api/admin/** requires ROLE_ADMIN
 * - Everything else requires authentication
 * - JwtAuthFilter runs before Spring's UsernamePasswordAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final AuthEntryPoint authEntryPoint;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("GET", "/api/locations/**").permitAll()
                        .requestMatchers("GET", "/api/roads/**").permitAll()
                        .requestMatchers("GET", "/api/route/**").permitAll()
                        .requestMatchers("GET", "/api/nearby/**").permitAll()
                        .requestMatchers("GET", "/api/search/**").permitAll()
                        .requestMatchers("GET", "/api/traffic/current").permitAll()
                        .requestMatchers("GET", "/api/analytics/**").permitAll()
                        .requestMatchers("POST", "/api/locations/**").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/locations/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/roads/**").hasRole("ADMIN")
                        .requestMatchers("PATCH", "/api/roads/**").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/roads/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/traffic/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("GET", "/api/history/**").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
