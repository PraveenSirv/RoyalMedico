package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.config.JwtProperties;
import Royal_Medico.auth_service.dto.AuthResponse;
import Royal_Medico.auth_service.dto.RegisterRequest;
import Royal_Medico.auth_service.dto.TenantAuthResponse;
import Royal_Medico.auth_service.dto.TenantMembershipDto;
import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.repository.RefreshTokenRepository;
import Royal_Medico.auth_service.repository.TenantMembershipRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import Royal_Medico.auth_service.repository.UserRepository;
import Royal_Medico.auth_service.util.HashUtil;
import Royal_Medico.auth_service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantMembershipServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMembershipRepository tenantMembershipRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HashUtil hashUtil;

    private JwtUtil jwtUtil;
    private JwtProperties jwtProperties;
    private AuthService authService;

    private User testUser;
    private Tenant tenant1;
    private Tenant tenant2;
    private TenantMembership membership1;
    private TenantMembership membership2;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setBase64Secret("NTQ2NzU2NkI1OTcwMzM3MzM2NzYzOTc5MjQ0MjI2NDUyOTQ4NDA0RDYzNTE2NjU0NkE1NzZFNUE3MjM0NzUzNw==");
        jwtProperties.setIssuer("royal-medico-test");
        jwtProperties.setAudience("royal-medico-gateway-test");
        jwtProperties.setAccessTokenExpirationMs(900000L);
        jwtProperties.setRefreshTokenValidityDays(7);

        jwtUtil = new JwtUtil(jwtProperties);

        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                tenantRepository,
                tenantMembershipRepository,
                passwordEncoder,
                jwtUtil,
                hashUtil,
                jwtProperties
        );

        testUser = new User();
        testUser.setId(100L);
        testUser.setEmail("pharmacy.owner@gmail.com");
        testUser.setName("Praveen Owner");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CUSTOMER);
        testUser.setActive(true);
        testUser.setAccountLocked(false);

        tenant1 = new Tenant("Apollo Pharmacy Main", "apollo-main");
        tenant1.setId(1L);
        tenant1.setStatus(TenantStatus.ACTIVE);

        tenant2 = new Tenant("MedPlus Central", "medplus-central");
        tenant2.setId(2L);
        tenant2.setStatus(TenantStatus.ACTIVE);

        membership1 = new TenantMembership(testUser, tenant1, TenantRole.OWNER, MembershipStatus.ACTIVE);
        membership1.setId(10L);

        membership2 = new TenantMembership(testUser, tenant2, TenantRole.OWNER, MembershipStatus.ACTIVE);
        membership2.setId(20L);
    }

    @Test
    @DisplayName("Public registration enforces Role.CUSTOMER regardless of requested role")
    void register_enforcesCustomerRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@gmail.com");
        request.setPassword("Password123!");
        request.setName("New Customer");
        request.setRole("ADMIN"); // Attempting to register as ADMIN

        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());

        // Verify that the user saved in DB was forced to CUSTOMER, not ADMIN
        verify(userRepository).save(argThat(user -> user.getRole() == Role.CUSTOMER));
    }

    @Test
    @DisplayName("User with no pharmacy membership returns empty list")
    void getUserMemberships_returnsEmpty_forCustomerWithoutMemberships() {
        String token = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + token;

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndStatus(testUser.getId(), MembershipStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        List<TenantMembershipDto> memberships = authService.getUserMemberships(authHeader);

        assertNotNull(memberships);
        assertTrue(memberships.isEmpty());
    }

    @Test
    @DisplayName("User with single pharmacy membership returns single item")
    void getUserMemberships_returnsSingle_forUserWithOneMembership() {
        String token = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + token;

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndStatus(testUser.getId(), MembershipStatus.ACTIVE))
                .thenReturn(List.of(membership1));

        List<TenantMembershipDto> memberships = authService.getUserMemberships(authHeader);

        assertEquals(1, memberships.size());
        assertEquals(1L, memberships.get(0).getTenantId());
        assertEquals("Apollo Pharmacy Main", memberships.get(0).getTenantName());
        assertEquals("apollo-main", memberships.get(0).getTenantSlug());
        assertEquals("OWNER", memberships.get(0).getRole());
        assertEquals("ACTIVE", memberships.get(0).getStatus());
    }

    @Test
    @DisplayName("User with multiple pharmacy memberships returns all active memberships")
    void getUserMemberships_returnsMultiple_forUserWithMultipleMemberships() {
        String token = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + token;

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndStatus(testUser.getId(), MembershipStatus.ACTIVE))
                .thenReturn(List.of(membership1, membership2));

        List<TenantMembershipDto> memberships = authService.getUserMemberships(authHeader);

        assertEquals(2, memberships.size());
        assertEquals(1L, memberships.get(0).getTenantId());
        assertEquals("OWNER", memberships.get(0).getRole());
        assertEquals(2L, memberships.get(1).getTenantId());
        assertEquals("OWNER", memberships.get(1).getRole());
    }

    @Test
    @DisplayName("Valid tenant selection issues tenant-scoped token with required claims")
    void selectTenant_success_issuesTenantScopedToken() {
        String globalToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + globalToken;

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndTenantId(testUser.getId(), 1L))
                .thenReturn(Optional.of(membership1));

        TenantAuthResponse response = authService.selectTenant(authHeader, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getTenantId());
        assertEquals("Apollo Pharmacy Main", response.getTenantName());
        assertEquals("apollo-main", response.getTenantSlug());
        assertEquals(10L, response.getMembershipId());
        assertEquals("OWNER", response.getTenantRole());

        // Validate JWT Claims
        String tenantToken = response.getAccessToken();
        assertTrue(jwtUtil.validateToken(tenantToken));

        Claims claims = jwtUtil.getClaims(tenantToken);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals("100", claims.get("userId"));
        assertEquals("1", claims.get("tenantId"));
        assertEquals("10", claims.get("membershipId"));
        assertEquals("OWNER", claims.get("role"));
        assertEquals("OWNER", claims.get("tenantRole"));
        assertEquals("TENANT", claims.get("type"));
        assertNotNull(claims.getId()); // jti
    }

    @Test
    @DisplayName("Tenant selection fails when user has no membership in requested tenant")
    void selectTenant_fails_whenNoMembership() {
        String globalToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + globalToken;

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndTenantId(testUser.getId(), 999L))
                .thenReturn(Optional.empty());

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authService.selectTenant(authHeader, 999L)
        );
        assertTrue(ex.getMessage().contains("Unauthorized: User has no membership in tenant 999"));
    }

    @Test
    @DisplayName("Tenant selection fails when membership is INACTIVE or SUSPENDED")
    void selectTenant_fails_whenMembershipInactive() {
        String globalToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + globalToken;

        membership1.setStatus(MembershipStatus.INACTIVE);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndTenantId(testUser.getId(), 1L))
                .thenReturn(Optional.of(membership1));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authService.selectTenant(authHeader, 1L)
        );
        assertTrue(ex.getMessage().contains("Tenant membership is INACTIVE"));
    }

    @Test
    @DisplayName("Tenant selection fails when tenant itself is SUSPENDED or CLOSED")
    void selectTenant_fails_whenTenantSuspended() {
        String globalToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + globalToken;

        tenant1.setStatus(TenantStatus.SUSPENDED);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndTenantId(testUser.getId(), 1L))
                .thenReturn(Optional.of(membership1));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                authService.selectTenant(authHeader, 1L)
        );
        assertTrue(ex.getMessage().contains("Tenant is SUSPENDED"));
    }

    @Test
    @DisplayName("Tenant switching allows user to switch from Tenant 1 to Tenant 2")
    void switchTenant_allowsSwitchingBetweenAuthorizedTenants() {
        // Step 1: User has Tenant 1 token
        String tenant1Token = jwtUtil.generateTenantAccessToken(
                testUser.getId(), testUser.getEmail(), 1L, 10L, "OWNER", List.of("ROLE_OWNER")
        );
        String authHeader = "Bearer " + tenant1Token;

        // Step 2: Switch to Tenant 2
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tenantMembershipRepository.findByUserIdAndTenantId(testUser.getId(), 2L))
                .thenReturn(Optional.of(membership2));

        TenantAuthResponse switchResponse = authService.selectTenant(authHeader, 2L);

        assertNotNull(switchResponse);
        assertEquals(2L, switchResponse.getTenantId());
        assertEquals("MedPlus Central", switchResponse.getTenantName());
        assertEquals("OWNER", switchResponse.getTenantRole());

        Claims claims = jwtUtil.getClaims(switchResponse.getAccessToken());
        assertEquals("2", claims.get("tenantId"));
        assertEquals("20", claims.get("membershipId"));
        assertEquals("OWNER", claims.get("role"));
    }

    @Test
    @DisplayName("Tenant selection fails with invalid or malformed token")
    void selectTenant_fails_withInvalidToken() {
        String invalidAuthHeader = "Bearer invalid.jwt.token";

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                authService.selectTenant(invalidAuthHeader, 1L)
        );
        assertTrue(ex.getMessage().contains("Invalid or expired token"));
    }

    @Test
    @DisplayName("Tenant selection fails when missing Authorization Bearer prefix")
    void selectTenant_fails_whenMissingBearerPrefix() {
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                authService.selectTenant("Basic dXNlcjpwYXNz", 1L)
        );
        assertTrue(ex.getMessage().contains("Missing or invalid Authorization header"));
    }

    @Test
    @DisplayName("Tenant selection fails when user account is disabled")
    void selectTenant_fails_whenUserDisabled() {
        String globalToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), "CUSTOMER", Collections.emptyList());
        String authHeader = "Bearer " + globalToken;

        testUser.setActive(false);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(LockedException.class, () ->
                authService.selectTenant(authHeader, 1L)
        );
    }
}

