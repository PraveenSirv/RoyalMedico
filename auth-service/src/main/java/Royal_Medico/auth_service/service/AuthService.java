package royal_medico.auth_service.service;

import royal_medico.auth_service.dto.AuthResponse;
import royal_medico.auth_service.dto.LoginRequest;
import royal_medico.auth_service.dto.RegisterRequest;
import royal_medico.auth_service.entity.RefreshToken;
import royal_medico.auth_service.entity.Role;
import royal_medico.auth_service.entity.User;
import royal_medico.auth_service.repository.RefreshTokenRepository;
import royal_medico.auth_service.repository.UserRepository;
import royal_medico.auth_service.util.HashUtil;
import royal_medico.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HashUtil hashUtil;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(Role.valueOf(request.getRole()));

        userRepository.save(user);

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials")); // Prevent enumeration

        if (!user.isActive()) {
            throw new LockedException("Account is disabled");
        }

        if (user.isAccountLocked()) {
            // Check if unlock duration has passed
            // For now, simplistic lock logic
            throw new LockedException("Account is locked due to too many failed attempts");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newAttempts);
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
            }
            userRepository.save(user);
            throw new BadCredentialsException("Invalid credentials"); // Prevent enumeration
        }

        // Successful login, reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        String hashedToken = hashUtil.hash(refreshTokenStr);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        
        // Rotate refresh token
        refreshTokenRepository.delete(refreshToken);
        
        return createAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null) {
            String hashedToken = hashUtil.hash(refreshTokenStr);
            refreshTokenRepository.findByToken(hashedToken).ifPresent(refreshTokenRepository::delete);
        }
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), 
                user.getEmail(), 
                user.getRole().name(),
                user.getRole().getAuthorities()
        );
        String refreshTokenStr = jwtUtil.generateRefreshToken();
        String hashedToken = hashUtil.hash(refreshTokenStr);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(hashedToken); // Store hash, not raw
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenStr); // Return raw token to user
    }
}
