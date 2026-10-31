package Royal_Medico.auth_service.controller;

import Royal_Medico.auth_service.dto.TenantAuthResponse;
import Royal_Medico.auth_service.dto.TenantMembershipDto;
import Royal_Medico.auth_service.dto.TenantSelectRequest;
import Royal_Medico.auth_service.exception.GlobalExceptionHandler;
import Royal_Medico.auth_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TenantContextControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/auth/memberships returns 200 OK with list of memberships")
    void getMemberships_returns200() throws Exception {
        TenantMembershipDto dto = TenantMembershipDto.builder()
                .membershipId(10L)
                .tenantId(1L)
                .tenantName("Apollo Pharmacy")
                .tenantSlug("apollo-pharmacy")
                .role("OWNER")
                .status("ACTIVE")
                .build();

        when(authService.getUserMemberships("Bearer valid.token")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/auth/memberships")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId", is(1)))
                .andExpect(jsonPath("$[0].tenantName", is("Apollo Pharmacy")))
                .andExpect(jsonPath("$[0].role", is("OWNER")));
    }

    @Test
    @DisplayName("POST /api/auth/select-tenant with authorized membership returns 200 and tenant token")
    void selectTenant_returns200() throws Exception {
        TenantSelectRequest request = new TenantSelectRequest(1L);
        TenantAuthResponse response = TenantAuthResponse.builder()
                .accessToken("header.tenantPayload.signature")
                .tokenType("Bearer")
                .expiresInMs(900000L)
                .tenantId(1L)
                .tenantName("Apollo Pharmacy")
                .tenantSlug("apollo-pharmacy")
                .membershipId(10L)
                .tenantRole("OWNER")
                .build();

        when(authService.selectTenant("Bearer valid.token", 1L)).thenReturn(response);

        mockMvc.perform(post("/api/auth/select-tenant")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("header.tenantPayload.signature")))
                .andExpect(jsonPath("$.tenantId", is(1)))
                .andExpect(jsonPath("$.tenantRole", is("OWNER")));
    }

    @Test
    @DisplayName("POST /api/auth/select-tenant with unauthorized tenant returns 403 Forbidden")
    void selectTenant_unauthorized_returns403() throws Exception {
        TenantSelectRequest request = new TenantSelectRequest(999L);

        when(authService.selectTenant(anyString(), anyLong()))
                .thenThrow(new AccessDeniedException("Unauthorized: User has no membership in tenant 999"));

        mockMvc.perform(post("/api/auth/select-tenant")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/auth/select-tenant with invalid token returns 401 Unauthorized")
    void selectTenant_invalidToken_returns401() throws Exception {
        TenantSelectRequest request = new TenantSelectRequest(1L);

        when(authService.selectTenant(anyString(), anyLong()))
                .thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(post("/api/auth/select-tenant")
                        .header("Authorization", "Bearer invalid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/auth/switch-tenant successfully switches tenant context")
    void switchTenant_returns200() throws Exception {
        TenantSelectRequest request = new TenantSelectRequest(2L);
        TenantAuthResponse response = TenantAuthResponse.builder()
                .accessToken("header.switchedPayload.signature")
                .tokenType("Bearer")
                .expiresInMs(900000L)
                .tenantId(2L)
                .tenantName("MedPlus Central")
                .tenantSlug("medplus-central")
                .membershipId(20L)
                .tenantRole("PHARMACY_STAFF")
                .build();

        when(authService.selectTenant("Bearer valid.token", 2L)).thenReturn(response);

        mockMvc.perform(post("/api/auth/switch-tenant")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("header.switchedPayload.signature")))
                .andExpect(jsonPath("$.tenantId", is(2)))
                .andExpect(jsonPath("$.tenantRole", is("PHARMACY_STAFF")));
    }
}

