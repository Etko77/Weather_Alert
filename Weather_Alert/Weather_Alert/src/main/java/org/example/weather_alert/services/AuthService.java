package org.example.weather_alert.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather_alert.dto.AuthResponse;
import org.example.weather_alert.dto.LoginRequest;
import org.example.weather_alert.entities.User;
import org.example.weather_alert.exception.InvalidCredentialsException;
import org.example.weather_alert.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    // Dependencies injected via constructor
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        try {
            // Create authentication token with credentials
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    );

            // Authenticate using Spring Security's AuthenticationManager
            // This calls our CustomUserDetailsService internally
            Authentication authentication = authenticationManager.authenticate(authToken);

            log.debug("Authentication successful for user: {}", request.getUsername());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Extract user details for response
            User user = (User) authentication.getPrincipal();

            // Build and return response
            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toSet()))
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new InvalidCredentialsException("Authentication failed: " + e.getMessage());
        }
    }
}
