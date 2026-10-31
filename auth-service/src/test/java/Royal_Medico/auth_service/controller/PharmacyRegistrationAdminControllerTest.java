package Royal_Medico.auth_service.controller;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.Role;
import Royal_Medico.auth_service.entity.User;
import Royal_Medico.auth_service.exception.BusinessIdentityCollisionException;
import Royal_Medico.auth_service.exception.GlobalExceptionHandler;
import Royal_Medico.auth_service.repository.UserRepository;
import Royal_Medico.auth_service.service.PharmacyRegistrationService;
import Royal_Medico.auth_service.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PharmacyRegistrationAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PharmacyRegistrationService registrationService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PharmacyRegistrationAdminController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User superAdminUser;
    private User customerUser;
    private PharmacyRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        superAdminUser = new User();
        superAdminUser.setId(1L);
        superAdminUser.setEmail("superadmin@royalmedico.com");
        superAdminUser.setRole(Role.SUPER_ADMIN);
        superAdminUser.setActive(true);

        customerUser = new User();
        customerUser.setId(2L);
        customerUser.setEmail("customer@royalmedico.com");
        customerUser.setRole(Role.CUSTOMER);
        customerUser.setActive(true);

        validRequest = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Apollo Health Private Limited")
                .tradeName("Apollo Pharmacy Chembur")
                .proposedSlug("apollo-pharmacy-chembur")
                .proposedOwnerName("Rajesh Sharma")
                .proposedOwnerEmail("rajesh.sharma@apollopharmacy.com")
                .proposedOwnerPhone("+919876543210")
                .addressLine1("Shop 4, Central Avenue")
                .city("Mumbai")
                .state("Maharashtra")
                .postalCode("400071")
                .gstin("27AABCA1234A1Z5")
                .pan("AABCA1234A")
                .drugLicenseForm20("MH-MZ4-123456")
                .drugLicenseForm21("MH-MZ4-123457")
                .fssai("11521012000123")
                .build();
    }

    private void mockSuperAdminAuth() {
        when(jwtUtil.validateToken("admin.token")).thenReturn(true);
        when(jwtUtil.extractEmail("admin.token")).thenReturn("superadmin@royalmedico.com");
        when(userRepository.findByEmail("superadmin@royalmedico.com")).thenReturn(Optional.of(superAdminUser));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations returns 201 Created on valid input")
    void initiateRegistration_validInput_returns201() throws Exception {
        mockSuperAdminAuth();

        PharmacyRegistrationResponse response = PharmacyRegistrationResponse.builder()
                .id(101L)
                .proposedSlug("apollo-pharmacy-chembur")
                .status("PENDING_VERIFICATION")
                .businessIdentities(List.of(
                        BusinessIdentityDto.builder().identifierType("GSTIN").identifierValue("27AABCA1234A1Z5").status("RESERVED").build(),
                        BusinessIdentityDto.builder().identifierType("BUSINESS_PAN").identifierValue("AABCA1234A").status("RESERVED").build()
                ))
                .build();

        when(registrationService.initiateRegistration(any(PharmacyRegistrationRequest.class), eq(superAdminUser)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.proposedSlug", is("apollo-pharmacy-chembur")))
                .andExpect(jsonPath("$.status", is("PENDING_VERIFICATION")))
                .andExpect(jsonPath("$.businessIdentities", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations with invalid GSTIN regex returns 400 Bad Request")
    void initiateRegistration_invalidGstin_returns400() throws Exception {
        validRequest.setGstin("INVALID_GSTIN_123");

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message.gstin", containsString("Invalid Indian GSTIN structural format")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations collision returns 409 Conflict")
    void initiateRegistration_collision_returns409() throws Exception {
        mockSuperAdminAuth();

        when(registrationService.initiateRegistration(any(), any()))
                .thenThrow(new BusinessIdentityCollisionException("GSTIN", "27AABCA1234A1Z5", "RESERVED", 88L));

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.identifierType", is("GSTIN")))
                .andExpect(jsonPath("$.identifierValue", is("27AABCA1234A1Z5")))
                .andExpect(jsonPath("$.conflictingStatus", is("RESERVED")))
                .andExpect(jsonPath("$.conflictingEntityId", is(88)));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations PAN collision returns 409 Conflict")
    void initiateRegistration_panCollision_returns409() throws Exception {
        mockSuperAdminAuth();

        when(registrationService.initiateRegistration(any(), any()))
                .thenThrow(new BusinessIdentityCollisionException("BUSINESS_PAN", "AABCA1234A", "ACTIVE", 42L));

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.identifierType", is("BUSINESS_PAN")))
                .andExpect(jsonPath("$.identifierValue", is("AABCA1234A")))
                .andExpect(jsonPath("$.conflictingStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.conflictingEntityId", is(42)));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations slug collision returns 409 Conflict")
    void initiateRegistration_slugCollision_returns409() throws Exception {
        mockSuperAdminAuth();

        when(registrationService.initiateRegistration(any(), any()))
                .thenThrow(new BusinessIdentityCollisionException("PROPOSED_SLUG", "apollo-pharmacy-chembur", "RESERVED", null));

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.identifierType", is("PROPOSED_SLUG")))
                .andExpect(jsonPath("$.identifierValue", is("apollo-pharmacy-chembur")))
                .andExpect(jsonPath("$.message", containsString("apollo-pharmacy-chembur")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations by non-SUPER_ADMIN (CUSTOMER) receives 403 Forbidden")
    void initiateRegistration_customerRole_returns403() throws Exception {
        when(jwtUtil.validateToken("customer.token")).thenReturn(true);
        when(jwtUtil.extractEmail("customer.token")).thenReturn("customer@royalmedico.com");
        when(userRepository.findByEmail("customer@royalmedico.com")).thenReturn(Optional.of(customerUser));

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer customer.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Only Super Admin")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations by PHARMACIST receives 403 Forbidden")
    void initiateRegistration_pharmacistRole_returns403() throws Exception {
        User pharmacist = new User();
        pharmacist.setId(4L);
        pharmacist.setEmail("pharmacist@royalmedico.com");
        pharmacist.setRole(Role.PHARMACIST);
        pharmacist.setActive(true);

        when(jwtUtil.validateToken("pharmacist.token")).thenReturn(true);
        when(jwtUtil.extractEmail("pharmacist.token")).thenReturn("pharmacist@royalmedico.com");
        when(userRepository.findByEmail("pharmacist@royalmedico.com")).thenReturn(Optional.of(pharmacist));

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer pharmacist.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Only Super Admin")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations with missing Authorization header receives 401 Unauthorized")
    void initiateRegistration_missingAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations with expired token receives 401 Unauthorized")
    void initiateRegistration_expiredToken_returns401() throws Exception {
        when(jwtUtil.validateToken("expired.token")).thenReturn(false);

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer expired.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations by non-admin CUSTOMER returns 403 Forbidden")
    void initiateRegistration_nonAdmin_returns403() throws Exception {
        when(jwtUtil.validateToken("customer.token")).thenReturn(true);
        when(jwtUtil.extractEmail("customer.token")).thenReturn("customer@royalmedico.com");
        when(userRepository.findByEmail("customer@royalmedico.com")).thenReturn(Optional.of(customerUser));

        mockMvc.perform(post("/api/admin/pharmacy-registrations")
                        .header("Authorization", "Bearer customer.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Only Super Admin")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations/{id}/verify returns 200 OK")
    void verifyRegistration_returns200() throws Exception {
        mockSuperAdminAuth();

        VerifyRegistrationRequest request = new VerifyRegistrationRequest("FDA e-Governance verified");
        PharmacyRegistrationResponse response = PharmacyRegistrationResponse.builder()
                .id(101L)
                .status("VERIFIED")
                .verificationNotes("FDA e-Governance verified")
                .build();

        when(registrationService.verifyRegistration(eq(101L), any(), eq(superAdminUser))).thenReturn(response);

        mockMvc.perform(post("/api/admin/pharmacy-registrations/101/verify")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.status", is("VERIFIED")));
    }

    @Test
    @DisplayName("POST /api/admin/pharmacy-registrations/{id}/reject returns 200 OK")
    void rejectRegistration_returns200() throws Exception {
        mockSuperAdminAuth();

        RejectRegistrationRequest request = new RejectRegistrationRequest("Cancelled by applicant request");
        PharmacyRegistrationResponse response = PharmacyRegistrationResponse.builder()
                .id(101L)
                .status("REJECTED")
                .rejectionReason("Cancelled by applicant request")
                .build();

        when(registrationService.rejectRegistration(eq(101L), any(), eq(superAdminUser))).thenReturn(response);

        mockMvc.perform(post("/api/admin/pharmacy-registrations/101/reject")
                        .header("Authorization", "Bearer admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    @DisplayName("GET /api/admin/pharmacy-registrations/{id} returns 200 OK")
    void getRegistration_returns200() throws Exception {
        mockSuperAdminAuth();

        PharmacyRegistrationResponse response = PharmacyRegistrationResponse.builder()
                .id(101L)
                .proposedSlug("apollo-pharmacy-chembur")
                .status("PENDING_VERIFICATION")
                .build();

        when(registrationService.getRegistration(101L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/pharmacy-registrations/101")
                        .header("Authorization", "Bearer admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.proposedSlug", is("apollo-pharmacy-chembur")));
    }
}

