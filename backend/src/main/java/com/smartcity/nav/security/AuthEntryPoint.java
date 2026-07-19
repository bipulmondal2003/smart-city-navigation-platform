package com.smartcity.nav.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcity.nav.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles unauthenticated access attempts to protected routes -
 * returns 401 with a JSON error body instead of Spring's default HTML page.
 *
 * Registered on HttpSecurity via .exceptionHandling(ex -> ex.authenticationEntryPoint(this)).
 */
@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {

    // Injects Spring Boot's auto-configured ObjectMapper bean, which already has
    // the JavaTimeModule registered (so LocalDateTime serializes correctly) -
    // a manually-constructed `new ObjectMapper()` does NOT have that module
    // registered by default, which was silently turning every 401 response
    // into a second, unrelated 500 error.
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .message("Unauthorized: authentication is required to access this resource")
                .timestamp(LocalDateTime.now())
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
