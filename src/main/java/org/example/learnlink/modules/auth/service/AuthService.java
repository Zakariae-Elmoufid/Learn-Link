package org.example.learnlink.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.event.OnUserRegisteredEvent;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.auth.security.JwtService;
import org.example.learnlink.modules.auth.dto.*;
import org.example.learnlink.modules.auth.exception.AuthenticationException;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.entity.UserRole;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verificationCode(UUID.randomUUID().toString())
                .role(UserRole.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);


        eventPublisher.publishEvent(new OnUserRegisteredEvent(savedUser));

    }
    public boolean verify(String code) {
        User user = userRepository.findByVerificationCode(code).orElse(null);
        if (user == null) return false;

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
        return true;
    }

    public AuthResponse login(LoginRequest request) {
        // First, find the user to check their status before authentication
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Check if email is verified
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthenticationException("Email not verified. Please check your inbox and verify your email before logging in.");
        }

        // Check if account is active
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AuthenticationException("Account is deactivated. Please contact support for assistance.");
        }

        // Now authenticate credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid email or password");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new AuthenticationException("Invalid refresh token");
        }

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new AuthenticationException("Refresh token is expired or invalid");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return buildAuthResponse(userDetails.getUser(), newAccessToken, newRefreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .role(user.getRole())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }
}
