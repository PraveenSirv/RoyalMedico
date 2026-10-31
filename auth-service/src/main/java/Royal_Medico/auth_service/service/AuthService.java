package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.config.JwtProperties;
import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.repository.RefreshTokenRepository;
import Royal_Medico.auth_service.repository.TenantMembershipRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import Royal_Medico.auth_service.repository.UserRepository;
import Royal_Medico.auth_service.util.HashUtil;
import Royal_Medico.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TenantRepository tenantRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HashUtil hashUtil;
    private final JwtProperties jwtProperties;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("AUTH_AUDIT_REGISTER_FAILED: Email already registered: {}", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        // Public registration strictly creates a global user account with CUSTOMER
        // capabilities only
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);
        log.info("AUTH_AUDIT_REGISTER_SUCCESS: User registered successfully - userId: {}, email: {}, role: {}",
                user.getId(), user.getEmail(), user.getRole());

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("AUTH_AUDIT_LOGIN_FAILED: User not found for email: {}", request.getEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        if (!user.isActive()) {
            log.warn("AUTH_AUDIT_LOGIN_BLOCKED: Account is disabled - userId: {}, email: {}", user.getId(),
                    user.getEmail());
            throw new LockedException("Account is disabled");
        }

        if (user.isAccountLocked()) {
            log.warn("AUTH_AUDIT_LOGIN_BLOCKED: Account is locked - userId: {}, email: {}", user.getId(),
                    user.getEmail());
            throw new LockedException("Account is locked due to too many failed attempts");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newAttempts);
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                log.warn(
                        "AUTH_AUDIT_ACCOUNT_LOCKOUT: Account locked due to {} consecutive failed attempts - userId: {}, email: {}",
                        MAX_FAILED_ATTEMPTS, user.getId(), user.getEmail());
            } else {
                log.warn("AUTH_AUDIT_LOGIN_FAILED: Bad credentials for email: {} - failed attempts: {}/{}",
                        request.getEmail(), newAttempts, MAX_FAILED_ATTEMPTS);
            }
            userRepository.save(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Successful login, reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        log.info("AUTH_AUDIT_LOGIN_SUCCESS: User logged in successfully - userId: {}, email: {}", user.getId(),
                user.getEmail());
        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        String hashedToken = hashUtil.hash(refreshTokenStr);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> {
                    log.warn("AUTH_AUDIT_REFRESH_FAILED: Invalid refresh token");
                    return new BadCredentialsException("Invalid refresh token");
                });

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("AUTH_AUDIT_REFRESH_FAILED: Refresh token expired for userId: {}", refreshToken.getUser().getId());
            throw new BadCredentialsException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        // Rotate refresh token
        refreshTokenRepository.delete(refreshToken);
        log.info("AUTH_AUDIT_REFRESH_SUCCESS: Refresh token rotated for userId: {}", user.getId());

        return createAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null) {
            String hashedToken = hashUtil.hash(refreshTokenStr);
            refreshTokenRepository.findByToken(hashedToken).ifPresent(token -> {
                refreshTokenRepository.delete(token);
                log.info("AUTH_AUDIT_LOGOUT: Refresh token revoked on logout for userId: {}", token.getUser().getId());
            });
        }
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getRole().getAuthorities());
        String refreshTokenStr = jwtUtil.generateRefreshToken();
        String hashedToken = hashUtil.hash(refreshTokenStr);

        // Invalidate all existing refresh tokens for this user before issuing a new one
        refreshTokenRepository.deleteByUser_Id(user.getId());

        long validityDays = jwtProperties != null ? jwtProperties.getRefreshTokenValidityDays() : 7;

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(hashedToken); // Store hash, not raw
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(validityDays));

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenStr); // Return raw token to user
    }

    @Transactional(readOnly = true)
    public List<TenantMembershipDto> getUserMemberships(String authHeader) {
        String token = parseBearerToken(authHeader);
        if (!jwtUtil.validateToken(token)) {
            log.warn("AUTH_AUDIT_MEMBERSHIPS_FAILED: Invalid or expired token");
            throw new BadCredentialsException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new LockedException("Account is disabled");
        }

        List<TenantMembership> memberships = tenantMembershipRepository.findByUserIdAndStatus(user.getId(),
                MembershipStatus.ACTIVE);
        return memberships.stream()
                .map(m -> TenantMembershipDto.builder()
                        .membershipId(m.getId())
                        .tenantId(m.getTenant().getId())
                        .tenantName(m.getTenant().getName())
                        .tenantSlug(m.getTenant().getSlug())
                        .role(m.getRole().name())
                        .status(m.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TenantAuthResponse selectTenant(String authHeader, Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }

        String token = parseBearerToken(authHeader);
        if (!jwtUtil.validateToken(token)) {
            log.warn("AUTH_AUDIT_TENANT_SELECT_FAILED: Invalid or expired token");
            throw new BadCredentialsException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            log.warn("AUTH_AUDIT_TENANT_SELECT_BLOCKED: Account is disabled for userId: {}", user.getId());
            throw new LockedException("Account is disabled");
        }

        if (user.isAccountLocked()) {
            log.warn("AUTH_AUDIT_TENANT_SELECT_BLOCKED: Account is locked for userId: {}", user.getId());
            throw new LockedException("Account is locked");
        }

        TenantMembership membership = tenantMembershipRepository.findByUserIdAndTenantId(user.getId(), tenantId)
                .orElseThrow(() -> {
                    log.warn("AUTH_AUDIT_TENANT_SELECT_UNAUTHORIZED: User {} has no membership in tenant {}", user.getId(), tenantId);
                    return new AccessDeniedException("Unauthorized: User has no membership in tenant " + tenantId);
                });

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            log.warn("AUTH_AUDIT_TENANT_SELECT_INACTIVE: Membership {} for user {} in tenant {} is {}",
                    membership.getId(), user.getId(), tenantId, membership.getStatus());
            throw new AccessDeniedException("Tenant membership is " + membership.getStatus());
        }

        Tenant tenant = membership.getTenant();
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            log.warn("AUTH_AUDIT_TENANT_SELECT_INACTIVE: Tenant {} ({}) status is {}", tenant.getId(), tenant.getName(), tenant.getStatus());
            throw new AccessDeniedException("Tenant is " + tenant.getStatus());
        }

        List<String> authorities = List.of("ROLE_" + membership.getRole().name());
        String tenantAccessToken = jwtUtil.generateTenantAccessToken(
                user.getId(),
                user.getEmail(),
                tenant.getId(),
                membership.getId(),
                membership.getRole().name(),
                authorities
        );

        log.info("AUTH_AUDIT_TENANT_SELECTED: User {} successfully selected tenant {} ({}) with role {}",
                user.getId(), tenant.getId(), tenant.getName(), membership.getRole());

        return TenantAuthResponse.builder()
                .accessToken(tenantAccessToken)
                .tokenType("Bearer")
                .expiresInMs(jwtUtil.getAccessTokenExpirationMs())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .tenantSlug(tenant.getSlug())
                .membershipId(membership.getId())
                .tenantRole(membership.getRole().name())
                .build();
    }

    private String parseBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        throw new BadCredentialsException("Missing or invalid Authorization header");
    }
}
