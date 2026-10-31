package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.exception.BusinessIdentityCollisionException;
import Royal_Medico.auth_service.repository.BusinessIdentityRepository;
import Royal_Medico.auth_service.repository.PharmacyRegistrationRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyRegistrationServiceTest {

    @Mock
    private PharmacyRegistrationRepository registrationRepository;

    @Mock
    private BusinessIdentityRepository identityRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private PharmacyRegistrationService registrationService;

    private User superAdminUser;
    private User normalCustomerUser;
    private PharmacyRegistrationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        superAdminUser = new User();
        superAdminUser.setId(1L);
        superAdminUser.setEmail("superadmin@royalmedico.com");
        superAdminUser.setRole(Role.SUPER_ADMIN);
        superAdminUser.setActive(true);

        normalCustomerUser = new User();
        normalCustomerUser.setId(2L);
        normalCustomerUser.setEmail("customer@royalmedico.com");
        normalCustomerUser.setRole(Role.CUSTOMER);
        normalCustomerUser.setActive(true);

        sampleRequest = PharmacyRegistrationRequest.builder()
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

    @Test
    @DisplayName("Super Admin successfully initiates registration with statutory normalization and identity reservation")
    void initiateRegistration_success() {
        when(tenantRepository.existsBySlug("apollo-pharmacy-chembur")).thenReturn(false);
        when(registrationRepository.existsByProposedSlug("apollo-pharmacy-chembur")).thenReturn(false);
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());

        when(registrationRepository.saveAndFlush(any(PharmacyRegistration.class))).thenAnswer(invocation -> {
            PharmacyRegistration reg = invocation.getArgument(0);
            reg.setId(100L);
            return reg;
        });

        PharmacyRegistrationResponse response = registrationService.initiateRegistration(sampleRequest, superAdminUser);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("apollo-pharmacy-chembur", response.getProposedSlug());
        assertEquals("PENDING_VERIFICATION", response.getStatus());

        // Verify that 5 statutory identities were reserved (GSTIN, PAN, Form 20, Form 21, FSSAI)
        assertEquals(5, response.getBusinessIdentities().size());

        // Verify normalization to uppercase
        assertTrue(response.getBusinessIdentities().stream()
                .anyMatch(bi -> bi.getIdentifierType().equals("GSTIN") && bi.getIdentifierValue().equals("27AABCA1234A1Z5")));
        assertTrue(response.getBusinessIdentities().stream()
                .anyMatch(bi -> bi.getIdentifierType().equals("BUSINESS_PAN") && bi.getIdentifierValue().equals("AABCA1234A")));
        assertTrue(response.getBusinessIdentities().stream()
                .allMatch(bi -> bi.getStatus().equals("RESERVED")));
    }

    @Test
    @DisplayName("Role model enforcement: Role.ADMIN does not exist in Role enum")
    void roleModelEnforcement_noAdminRoleExists() {
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("ADMIN"),
                "ADMIN role must not exist in global Role enum");

        List<String> roleNames = java.util.Arrays.stream(Role.values())
                .map(Enum::name)
                .toList();

        assertEquals(4, roleNames.size());
        assertTrue(roleNames.contains("SUPER_ADMIN"));
        assertTrue(roleNames.contains("PHARMACIST"));
        assertTrue(roleNames.contains("CUSTOMER"));
        assertTrue(roleNames.contains("DELIVERY_AGENT"));
        assertFalse(roleNames.contains("ADMIN"));
    }

    @Test
    @DisplayName("Non-Super-Admin user (CUSTOMER, PHARMACIST, DELIVERY_AGENT) cannot initiate registration")
    void initiateRegistration_nonSuperAdmin_throwsAccessDenied() {
        // Customer
        assertThrows(AccessDeniedException.class, () -> 
                registrationService.initiateRegistration(sampleRequest, normalCustomerUser));

        // Pharmacist
        User pharmacistUser = new User();
        pharmacistUser.setId(3L);
        pharmacistUser.setEmail("pharmacist@royalmedico.com");
        pharmacistUser.setRole(Role.PHARMACIST);
        pharmacistUser.setActive(true);
        assertThrows(AccessDeniedException.class, () -> 
                registrationService.initiateRegistration(sampleRequest, pharmacistUser));

        // Delivery Agent
        User deliveryUser = new User();
        deliveryUser.setId(4L);
        deliveryUser.setEmail("delivery@royalmedico.com");
        deliveryUser.setRole(Role.DELIVERY_AGENT);
        deliveryUser.setActive(true);
        assertThrows(AccessDeniedException.class, () -> 
                registrationService.initiateRegistration(sampleRequest, deliveryUser));

        verify(registrationRepository, never()).save(any());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Collision detection rejects registration when GSTIN is already RESERVED by a pending application")
    void initiateRegistration_duplicateReservedGstin_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        PharmacyRegistration existingReg = new PharmacyRegistration();
        existingReg.setId(88L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.GSTIN);
        existingIdentity.setIdentifierValue("27AABCA1234A1Z5");
        existingIdentity.setStatus(BusinessIdentityStatus.RESERVED);
        existingIdentity.setRegistration(existingReg);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(
                BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("GSTIN", ex.getIdentifierType());
        assertEquals("27AABCA1234A1Z5", ex.getIdentifierValue());
        assertEquals("RESERVED", ex.getExistingStatus());
        assertEquals(88L, ex.getConflictingEntityId());

        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Collision detection rejects registration when PAN is already ACTIVE on an existing tenant")
    void initiateRegistration_duplicateActivePan_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        Tenant existingTenant = new Tenant("Existing Chemist", "existing-chemist");
        existingTenant.setId(42L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.BUSINESS_PAN);
        existingIdentity.setIdentifierValue("AABCA1234A");
        existingIdentity.setStatus(BusinessIdentityStatus.ACTIVE);
        existingIdentity.setTenant(existingTenant);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5"))
                .thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.BUSINESS_PAN, "AABCA1234A"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("BUSINESS_PAN", ex.getIdentifierType());
        assertEquals("ACTIVE", ex.getExistingStatus());
        assertEquals(42L, ex.getConflictingEntityId());
    }

    @Test
    @DisplayName("Collision detection rejects registration when DRUG_LICENCE_FORM_20 is RESERVED (with lowercase/whitespace input)")
    void initiateRegistration_duplicateReservedForm20_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        PharmacyRegistration existingReg = new PharmacyRegistration();
        existingReg.setId(77L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.DRUG_LICENCE_FORM_20);
        existingIdentity.setIdentifierValue("MH-MZ4-123456");
        existingIdentity.setStatus(BusinessIdentityStatus.RESERVED);
        existingIdentity.setRegistration(existingReg);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5"))
                .thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.BUSINESS_PAN, "AABCA1234A"))
                .thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_20, "MH-MZ4-123456"))
                .thenReturn(Optional.of(existingIdentity));

        // Input with lowercase and whitespace
        sampleRequest.setDrugLicenseForm20("   mh-mz4-123456   ");

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("DRUG_LICENCE_FORM_20", ex.getIdentifierType());
        assertEquals("MH-MZ4-123456", ex.getIdentifierValue());
        assertEquals("RESERVED", ex.getExistingStatus());
        assertEquals(77L, ex.getConflictingEntityId());
    }

    @Test
    @DisplayName("Collision detection rejects registration when DRUG_LICENCE_FORM_21 is ACTIVE on tenant")
    void initiateRegistration_duplicateActiveForm21_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        Tenant existingTenant = new Tenant("Alpha Chemist", "alpha-chemist");
        existingTenant.setId(99L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.DRUG_LICENCE_FORM_21);
        existingIdentity.setIdentifierValue("MH-MZ4-123457");
        existingIdentity.setStatus(BusinessIdentityStatus.ACTIVE);
        existingIdentity.setTenant(existingTenant);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.BUSINESS_PAN, "AABCA1234A")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_20, "MH-MZ4-123456")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_21, "MH-MZ4-123457"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("DRUG_LICENCE_FORM_21", ex.getIdentifierType());
        assertEquals("ACTIVE", ex.getExistingStatus());
        assertEquals(99L, ex.getConflictingEntityId());
    }

    @Test
    @DisplayName("Collision detection rejects registration when DRUG_LICENCE_FORM_20B (Wholesale) is RESERVED")
    void initiateRegistration_duplicateReservedForm20B_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        sampleRequest.setDrugLicenseForm20B("mh-mz4-20b-888");

        PharmacyRegistration existingReg = new PharmacyRegistration();
        existingReg.setId(55L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.DRUG_LICENCE_FORM_20B);
        existingIdentity.setIdentifierValue("MH-MZ4-20B-888");
        existingIdentity.setStatus(BusinessIdentityStatus.RESERVED);
        existingIdentity.setRegistration(existingReg);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.BUSINESS_PAN, "AABCA1234A")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_20, "MH-MZ4-123456")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_21, "MH-MZ4-123457")).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_20B, "MH-MZ4-20B-888"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("DRUG_LICENCE_FORM_20B", ex.getIdentifierType());
        assertEquals("MH-MZ4-20B-888", ex.getIdentifierValue());
        assertEquals("RESERVED", ex.getExistingStatus());
    }

    @Test
    @DisplayName("Collision detection rejects registration when DRUG_LICENCE_FORM_21B (Wholesale) is ACTIVE")
    void initiateRegistration_duplicateActiveForm21B_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        sampleRequest.setDrugLicenseForm21B("mh-mz4-21b-888");

        Tenant existingTenant = new Tenant("Wholesale Depot", "wholesale-depot");
        existingTenant.setId(66L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.DRUG_LICENCE_FORM_21B);
        existingIdentity.setIdentifierValue("MH-MZ4-21B-888");
        existingIdentity.setStatus(BusinessIdentityStatus.ACTIVE);
        existingIdentity.setTenant(existingTenant);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_21B, "MH-MZ4-21B-888"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("DRUG_LICENCE_FORM_21B", ex.getIdentifierType());
        assertEquals("ACTIVE", ex.getExistingStatus());
        assertEquals(66L, ex.getConflictingEntityId());
    }

    @Test
    @DisplayName("Collision detection rejects registration when DRUG_LICENCE_FORM_20C (Homoeopathic) is RESERVED")
    void initiateRegistration_duplicateReservedForm20C_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        sampleRequest.setDrugLicenseForm20C("mh-mz4-20c-777");

        PharmacyRegistration existingReg = new PharmacyRegistration();
        existingReg.setId(33L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.DRUG_LICENCE_FORM_20C);
        existingIdentity.setIdentifierValue("MH-MZ4-20C-777");
        existingIdentity.setStatus(BusinessIdentityStatus.RESERVED);
        existingIdentity.setRegistration(existingReg);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.DRUG_LICENCE_FORM_20C, "MH-MZ4-20C-777"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("DRUG_LICENCE_FORM_20C", ex.getIdentifierType());
        assertEquals("RESERVED", ex.getExistingStatus());
        assertEquals(33L, ex.getConflictingEntityId());
    }

    @Test
    @DisplayName("Collision detection rejects registration when FSSAI 14-digit licence is ACTIVE")
    void initiateRegistration_duplicateActiveFssai_throwsCollisionException() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);

        Tenant existingTenant = new Tenant("Nutra Store", "nutra-store");
        existingTenant.setId(44L);

        BusinessIdentity existingIdentity = new BusinessIdentity();
        existingIdentity.setIdentifierType(BusinessIdentifierType.FSSAI);
        existingIdentity.setIdentifierValue("11521012000123");
        existingIdentity.setStatus(BusinessIdentityStatus.ACTIVE);
        existingIdentity.setTenant(existingTenant);

        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType.FSSAI, "11521012000123"))
                .thenReturn(Optional.of(existingIdentity));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("FSSAI", ex.getIdentifierType());
        assertEquals("ACTIVE", ex.getExistingStatus());
        assertEquals(44L, ex.getConflictingEntityId());
    }

    @Test
    @DisplayName("Fix M1: Concurrent PAN collision during save correctly reports BUSINESS_PAN (not GSTIN)")
    void initiateRegistration_concurrentPanCollisionDuringSave_reportsPanConflict() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());

        // Simulate concurrent save collision where message contains BUSINESS_PAN
        when(registrationRepository.saveAndFlush(any())).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uk_active_business_identity\" Details: Key (identifier_type, identifier_value)=(BUSINESS_PAN, AABCA1234A) already exists."));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("BUSINESS_PAN", ex.getIdentifierType());
        assertEquals("AABCA1234A", ex.getIdentifierValue());
    }

    @Test
    @DisplayName("Fix M1: Concurrent slug collision during save correctly reports PROPOSED_SLUG")
    void initiateRegistration_concurrentSlugCollisionDuringSave_reportsSlugConflict() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());

        when(registrationRepository.saveAndFlush(any())).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"pharmacy_registrations_proposed_slug_key\" Details: Key (proposed_slug)=(apollo-pharmacy-chembur) already exists."));

        BusinessIdentityCollisionException ex = assertThrows(BusinessIdentityCollisionException.class, () ->
                registrationService.initiateRegistration(sampleRequest, superAdminUser));

        assertEquals("PROPOSED_SLUG", ex.getIdentifierType());
        assertEquals("apollo-pharmacy-chembur", ex.getIdentifierValue());
    }

    @Test
    @DisplayName("Super Admin successfully verifies pharmacy registration and statutory identities")
    void verifyRegistration_success() {
        PharmacyRegistration reg = new PharmacyRegistration();
        reg.setId(50L);
        reg.setStatus(RegistrationStatus.PENDING_VERIFICATION);
        reg.setProposedSlug("noble-chemist");

        BusinessIdentity identity = new BusinessIdentity(
                BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5", BusinessIdentityEntityType.REGISTRATION, reg);
        reg.addBusinessIdentity(identity);

        when(registrationRepository.findById(50L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(PharmacyRegistration.class))).thenAnswer(i -> i.getArgument(0));

        VerifyRegistrationRequest verifyReq = new VerifyRegistrationRequest("FDA Maharashtra Form 20 verified on e-Governance portal.");
        PharmacyRegistrationResponse response = registrationService.verifyRegistration(50L, verifyReq, superAdminUser);

        assertEquals("VERIFIED", response.getStatus());
        assertEquals("FDA Maharashtra Form 20 verified on e-Governance portal.", response.getVerificationNotes());
        assertEquals(1L, response.getVerifiedByUserId());
        assertNotNull(response.getVerifiedAt());

        // Verify identity verification timestamp updated
        assertNotNull(identity.getVerifiedAt());
        assertEquals(superAdminUser, identity.getVerifiedBy());
    }

    @Test
    @DisplayName("Super Admin rejects registration and releases reserved statutory identities")
    void rejectRegistration_releasesIdentities() {
        PharmacyRegistration reg = new PharmacyRegistration();
        reg.setId(50L);
        reg.setStatus(RegistrationStatus.PENDING_VERIFICATION);

        BusinessIdentity identity1 = new BusinessIdentity(
                BusinessIdentifierType.GSTIN, "27AABCA1234A1Z5", BusinessIdentityEntityType.REGISTRATION, reg);
        identity1.setStatus(BusinessIdentityStatus.RESERVED);

        BusinessIdentity identity2 = new BusinessIdentity(
                BusinessIdentifierType.BUSINESS_PAN, "AABCA1234A", BusinessIdentityEntityType.REGISTRATION, reg);
        identity2.setStatus(BusinessIdentityStatus.RESERVED);

        reg.addBusinessIdentity(identity1);
        reg.addBusinessIdentity(identity2);

        when(registrationRepository.findById(50L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(PharmacyRegistration.class))).thenAnswer(i -> i.getArgument(0));

        RejectRegistrationRequest rejectReq = new RejectRegistrationRequest("Fraudulent FDA Drug Licence number detected");
        PharmacyRegistrationResponse response = registrationService.rejectRegistration(50L, rejectReq, superAdminUser);

        assertEquals("REJECTED", response.getStatus());
        assertEquals("Fraudulent FDA Drug Licence number detected", response.getRejectionReason());

        // Assert all reserved identities were atomically RELEASED
        assertEquals(BusinessIdentityStatus.RELEASED, identity1.getStatus());
        assertEquals(BusinessIdentityStatus.RELEASED, identity2.getStatus());
    }

    @Test
    @DisplayName("Comprehensive 8 statutory identifiers: retail, wholesale, homoeopathic, GSTIN, PAN, and FSSAI")
    void initiateRegistration_allEightStatutoryIdentifiers_reservedAndNormalized() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(registrationRepository.existsByProposedSlug(anyString())).thenReturn(false);
        when(identityRepository.findByIdentifierTypeAndIdentifierValue(any(), any())).thenReturn(Optional.empty());

        when(registrationRepository.saveAndFlush(any(PharmacyRegistration.class))).thenAnswer(i -> {
            PharmacyRegistration reg = i.getArgument(0);
            reg.setId(200L);
            return reg;
        });

        sampleRequest.setDrugLicenseForm20B("mh-mz4-20b-999");
        sampleRequest.setDrugLicenseForm21B("mh-mz4-21b-999");
        sampleRequest.setDrugLicenseForm20C("mh-mz4-20c-999");

        PharmacyRegistrationResponse response = registrationService.initiateRegistration(sampleRequest, superAdminUser);

        assertNotNull(response);
        // All 8 statutory identifiers reserved
        assertEquals(8, response.getBusinessIdentities().size());

        // Verify all 8 normalized to uppercase
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("GSTIN") && bi.getIdentifierValue().equals("27AABCA1234A1Z5")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("BUSINESS_PAN") && bi.getIdentifierValue().equals("AABCA1234A")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("DRUG_LICENCE_FORM_20") && bi.getIdentifierValue().equals("MH-MZ4-123456")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("DRUG_LICENCE_FORM_21") && bi.getIdentifierValue().equals("MH-MZ4-123457")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("DRUG_LICENCE_FORM_20B") && bi.getIdentifierValue().equals("MH-MZ4-20B-999")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("DRUG_LICENCE_FORM_21B") && bi.getIdentifierValue().equals("MH-MZ4-21B-999")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("DRUG_LICENCE_FORM_20C") && bi.getIdentifierValue().equals("MH-MZ4-20C-999")));
        assertTrue(response.getBusinessIdentities().stream().anyMatch(bi -> bi.getIdentifierType().equals("FSSAI") && bi.getIdentifierValue().equals("11521012000123")));
    }
}
