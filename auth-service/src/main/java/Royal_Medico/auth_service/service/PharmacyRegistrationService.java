package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.exception.BusinessIdentityCollisionException;
import Royal_Medico.auth_service.repository.BusinessIdentityRepository;
import Royal_Medico.auth_service.repository.PharmacyRegistrationRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PharmacyRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(PharmacyRegistrationService.class);

    private final PharmacyRegistrationRepository registrationRepository;
    private final BusinessIdentityRepository identityRepository;
    private final TenantRepository tenantRepository;

    private static final Set<BusinessIdentityStatus> ACTIVE_OR_RESERVED = Set.of(
            BusinessIdentityStatus.RESERVED,
            BusinessIdentityStatus.ACTIVE);

    @Autowired
    public PharmacyRegistrationService(
            PharmacyRegistrationRepository registrationRepository,
            BusinessIdentityRepository identityRepository,
            TenantRepository tenantRepository) {
        this.registrationRepository = registrationRepository;
        this.identityRepository = identityRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public PharmacyRegistrationResponse initiateRegistration(PharmacyRegistrationRequest request, User adminUser) {
        validateSuperAdmin(adminUser);

        log.info("PHARMACY_REGISTRATION_INITIATE: Admin userId={} initiated registration for slug={}",
                adminUser.getId(), request.getProposedSlug());

        // 1. Validate proposed slug uniqueness across existing tenants and pending
        // registrations
        if (tenantRepository.existsBySlug(request.getProposedSlug())) {
            throw new IllegalArgumentException(
                    "Pharmacy slug '" + request.getProposedSlug() + "' is already in use by an active tenant");
        }
        if (registrationRepository.existsByProposedSlug(request.getProposedSlug())) {
            throw new IllegalArgumentException(
                    "Pharmacy slug '" + request.getProposedSlug() + "' is already claimed by a pending registration");
        }

        // 2. Normalize statutory identifiers (UPPER(TRIM()))
        String normalizedGstin = BusinessIdentity.normalize(request.getGstin());
        String normalizedPan = BusinessIdentity.normalize(request.getPan());
        String normalizedDrug20 = BusinessIdentity.normalize(request.getDrugLicenseForm20());
        String normalizedDrug21 = BusinessIdentity.normalize(request.getDrugLicenseForm21());
        String normalizedDrug20B = BusinessIdentity.normalize(request.getDrugLicenseForm20B());
        String normalizedDrug21B = BusinessIdentity.normalize(request.getDrugLicenseForm21B());
        String normalizedDrug20C = BusinessIdentity.normalize(request.getDrugLicenseForm20C());
        String normalizedFssai = BusinessIdentity.normalize(request.getFssai());

        // 3. Collision Detection against Common Business Identity Registry (RESERVED
        // and ACTIVE)
        checkIdentifierCollision(BusinessIdentifierType.GSTIN, normalizedGstin);
        checkIdentifierCollision(BusinessIdentifierType.BUSINESS_PAN, normalizedPan);
        checkIdentifierCollision(BusinessIdentifierType.DRUG_LICENCE_FORM_20, normalizedDrug20);

        if (normalizedDrug21 != null && !normalizedDrug21.isBlank()) {
            checkIdentifierCollision(BusinessIdentifierType.DRUG_LICENCE_FORM_21, normalizedDrug21);
        }
        if (normalizedDrug20B != null && !normalizedDrug20B.isBlank()) {
            checkIdentifierCollision(BusinessIdentifierType.DRUG_LICENCE_FORM_20B, normalizedDrug20B);
        }
        if (normalizedDrug21B != null && !normalizedDrug21B.isBlank()) {
            checkIdentifierCollision(BusinessIdentifierType.DRUG_LICENCE_FORM_21B, normalizedDrug21B);
        }
        if (normalizedDrug20C != null && !normalizedDrug20C.isBlank()) {
            checkIdentifierCollision(BusinessIdentifierType.DRUG_LICENCE_FORM_20C, normalizedDrug20C);
        }
        if (normalizedFssai != null && !normalizedFssai.isBlank()) {
            checkIdentifierCollision(BusinessIdentifierType.FSSAI, normalizedFssai);
        }

        // 4. Create PharmacyRegistration record
        PharmacyRegistration registration = new PharmacyRegistration();
        registration.setLegalBusinessName(request.getLegalBusinessName().trim());
        registration.setTradeName(request.getTradeName().trim());
        registration.setProposedSlug(request.getProposedSlug().trim().toLowerCase());
        registration.setStatus(RegistrationStatus.PENDING_VERIFICATION);
        registration.setProposedOwnerEmail(request.getProposedOwnerEmail().trim().toLowerCase());
        registration.setProposedOwnerName(request.getProposedOwnerName().trim());
        if (request.getProposedOwnerPhone() != null && !request.getProposedOwnerPhone().isBlank()) {
            registration.setProposedOwnerPhone(request.getProposedOwnerPhone().trim());
        }
        registration.setAddressLine1(request.getAddressLine1().trim());
        registration.setCity(request.getCity().trim());
        if (request.getState() != null && !request.getState().isBlank()) {
            registration.setState(request.getState().trim());
        } else {
            registration.setState("Maharashtra");
        }
        registration.setPostalCode(request.getPostalCode().trim());
        registration.setCreatedBy(adminUser.getEmail());

        List<BusinessIdentity> candidateIdentities = new ArrayList<>();
        BusinessIdentity gstinId = new BusinessIdentity(
                BusinessIdentifierType.GSTIN, normalizedGstin, BusinessIdentityEntityType.REGISTRATION, registration);
        BusinessIdentity panId = new BusinessIdentity(
                BusinessIdentifierType.BUSINESS_PAN, normalizedPan, BusinessIdentityEntityType.REGISTRATION,
                registration);
        BusinessIdentity drug20Id = new BusinessIdentity(
                BusinessIdentifierType.DRUG_LICENCE_FORM_20, normalizedDrug20, BusinessIdentityEntityType.REGISTRATION,
                registration);

        candidateIdentities.add(gstinId);
        candidateIdentities.add(panId);
        candidateIdentities.add(drug20Id);
        registration.addBusinessIdentity(gstinId);
        registration.addBusinessIdentity(panId);
        registration.addBusinessIdentity(drug20Id);

        if (normalizedDrug21 != null && !normalizedDrug21.isBlank()) {
            BusinessIdentity bi = new BusinessIdentity(
                    BusinessIdentifierType.DRUG_LICENCE_FORM_21, normalizedDrug21,
                    BusinessIdentityEntityType.REGISTRATION, registration);
            candidateIdentities.add(bi);
            registration.addBusinessIdentity(bi);
        }
        if (normalizedDrug20B != null && !normalizedDrug20B.isBlank()) {
            BusinessIdentity bi = new BusinessIdentity(
                    BusinessIdentifierType.DRUG_LICENCE_FORM_20B, normalizedDrug20B,
                    BusinessIdentityEntityType.REGISTRATION, registration);
            candidateIdentities.add(bi);
            registration.addBusinessIdentity(bi);
        }
        if (normalizedDrug21B != null && !normalizedDrug21B.isBlank()) {
            BusinessIdentity bi = new BusinessIdentity(
                    BusinessIdentifierType.DRUG_LICENCE_FORM_21B, normalizedDrug21B,
                    BusinessIdentityEntityType.REGISTRATION, registration);
            candidateIdentities.add(bi);
            registration.addBusinessIdentity(bi);
        }
        if (normalizedDrug20C != null && !normalizedDrug20C.isBlank()) {
            BusinessIdentity bi = new BusinessIdentity(
                    BusinessIdentifierType.DRUG_LICENCE_FORM_20C, normalizedDrug20C,
                    BusinessIdentityEntityType.REGISTRATION, registration);
            candidateIdentities.add(bi);
            registration.addBusinessIdentity(bi);
        }
        if (normalizedFssai != null && !normalizedFssai.isBlank()) {
            BusinessIdentity bi = new BusinessIdentity(
                    BusinessIdentifierType.FSSAI, normalizedFssai, BusinessIdentityEntityType.REGISTRATION,
                    registration);
            candidateIdentities.add(bi);
            registration.addBusinessIdentity(bi);
        }

        try {
            PharmacyRegistration saved = registrationRepository.saveAndFlush(registration);
            log.info("PHARMACY_REGISTRATION_SUCCESS: Registration id={} created with {} reserved identities",
                    saved.getId(), saved.getBusinessIdentities().size());
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            handleDataIntegrityViolation(ex, request, candidateIdentities);
            throw ex; // unreachable as handleDataIntegrityViolation throws, but satisfies compiler
        }
    }

    private void handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            PharmacyRegistrationRequest request,
            List<BusinessIdentity> candidateIdentities) {

        log.warn(
                "BUSINESS_IDENTITY_CONCURRENT_COLLISION: Database constraint violation during registration save for slug={}: {}",
                request.getProposedSlug(), ex.getMessage());

        String constraintName = extractConstraintName(ex);
        String detailMessage = extractDetailMessage(ex);
        String fullMessage = buildFullExceptionMessage(ex);

        // 1. Check for slug collision via constraint name, detail, or message
        if ((constraintName != null && constraintName.toLowerCase().contains("slug")) ||
                (detailMessage != null && detailMessage.toLowerCase().contains("slug")) ||
                (fullMessage != null && (fullMessage.toLowerCase().contains("proposed_slug") ||
                        fullMessage.contains(request.getProposedSlug())))) {
            throw new BusinessIdentityCollisionException("PROPOSED_SLUG", request.getProposedSlug(), "RESERVED", null);
        }

        // 2. Check if detailMessage or fullMessage contains any candidate's exact
        // identifier value
        if (fullMessage != null) {
            for (BusinessIdentity candidate : candidateIdentities) {
                if (candidate.getIdentifierValue() != null && fullMessage.contains(candidate.getIdentifierValue())) {
                    throw new BusinessIdentityCollisionException(
                            candidate.getIdentifierType().name(),
                            candidate.getIdentifierValue(),
                            "RESERVED",
                            null);
                }
            }
            // Check if specific identifier type name is in exception details
            for (BusinessIdentifierType type : BusinessIdentifierType.values()) {
                if (fullMessage.contains(type.name())) {
                    String value = extractCandidateValue(type, candidateIdentities);
                    throw new BusinessIdentityCollisionException(type.name(), value, "RESERVED", null);
                }
            }
        }

        // 3. Fallback: Extract GSTIN value from candidates if present, preserving HTTP
        // 409
        String gstinVal = extractCandidateValue(BusinessIdentifierType.GSTIN, candidateIdentities);
        throw new BusinessIdentityCollisionException(
                BusinessIdentifierType.GSTIN.name(), gstinVal, "RESERVED", null);
    }

    private String buildFullExceptionMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(" ");
            }
            current = current.getCause();
        }
        return sb.toString();
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
                if (cve.getConstraintName() != null && !cve.getConstraintName().isBlank()) {
                    return cve.getConstraintName();
                }
                java.sql.SQLException sqlEx = cve.getSQLException();
                if (sqlEx != null) {
                    String pgConstraint = extractPgServerErrorMessageField(sqlEx, "getConstraint");
                    if (pgConstraint != null) {
                        return pgConstraint;
                    }
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    private String extractDetailMessage(DataIntegrityViolationException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
                java.sql.SQLException sqlEx = cve.getSQLException();
                if (sqlEx != null) {
                    String pgDetail = extractPgServerErrorMessageField(sqlEx, "getDetail");
                    if (pgDetail != null) {
                        return pgDetail;
                    }
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    private String extractPgServerErrorMessageField(java.sql.SQLException sqlEx, String methodName) {
        try {
            java.lang.reflect.Method getServerErrorMessage = sqlEx.getClass().getMethod("getServerErrorMessage");
            Object serverErrorMessage = getServerErrorMessage.invoke(sqlEx);
            if (serverErrorMessage != null) {
                java.lang.reflect.Method fieldMethod = serverErrorMessage.getClass().getMethod(methodName);
                Object result = fieldMethod.invoke(serverErrorMessage);
                return result != null ? result.toString() : null;
            }
        } catch (Exception ignored) {
            // Not a PostgreSQL driver or method not available
        }
        return null;
    }

    private String extractCandidateValue(BusinessIdentifierType type, List<BusinessIdentity> identities) {
        for (BusinessIdentity bi : identities) {
            if (bi.getIdentifierType() == type) {
                return bi.getIdentifierValue();
            }
        }
        return null;
    }

    @Transactional
    public PharmacyRegistrationResponse verifyRegistration(Long registrationId, VerifyRegistrationRequest request,
            User adminUser) {
        validateSuperAdmin(adminUser);

        PharmacyRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy registration not found: " + registrationId));

        if (registration.getStatus() != RegistrationStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException(
                    "Only registrations in PENDING_VERIFICATION status can be verified. Current status: "
                            + registration.getStatus());
        }

        registration.setStatus(RegistrationStatus.VERIFIED);
        registration.setVerifiedBy(adminUser);
        registration.setVerifiedAt(LocalDateTime.now());
        registration.setVerificationNotes(request.getVerificationNotes().trim());

        // Update verification on associated business identities
        for (BusinessIdentity identity : registration.getBusinessIdentities()) {
            identity.setVerifiedAt(LocalDateTime.now());
            identity.setVerifiedBy(adminUser);
        }

        PharmacyRegistration saved = registrationRepository.save(registration);
        log.info("PHARMACY_REGISTRATION_VERIFIED: Registration id={} verified by admin userId={}",
                saved.getId(), adminUser.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public PharmacyRegistrationResponse rejectRegistration(Long registrationId, RejectRegistrationRequest request,
            User adminUser) {
        validateSuperAdmin(adminUser);

        PharmacyRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy registration not found: " + registrationId));

        if (registration.getStatus() != RegistrationStatus.PENDING_VERIFICATION &&
                registration.getStatus() != RegistrationStatus.VERIFIED) {
            throw new IllegalStateException("Cannot reject registration in status: " + registration.getStatus());
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setRejectionReason(request.getRejectionReason().trim());

        // Atomically release all reserved identities so they become available for
        // subsequent applications
        for (BusinessIdentity identity : registration.getBusinessIdentities()) {
            identity.setStatus(BusinessIdentityStatus.RELEASED);
        }

        PharmacyRegistration saved = registrationRepository.save(registration);
        log.info(
                "PHARMACY_REGISTRATION_REJECTED: Registration id={} rejected by admin userId={}. Reserved identities RELEASED.",
                saved.getId(), adminUser.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PharmacyRegistrationResponse getRegistration(Long registrationId) {
        PharmacyRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy registration not found: " + registrationId));

        return mapToResponse(registration);
    }

    @Transactional(readOnly = true)
    public List<PharmacyRegistrationResponse> listRegistrations(RegistrationStatus status) {
        List<PharmacyRegistration> registrationsList;
        if (status != null) {
            registrationsList = registrationRepository.findByStatus(status);
        } else {
            registrationsList = registrationRepository.findAll();
        }

        List<PharmacyRegistrationResponse> responses = new ArrayList<>();
        for (PharmacyRegistration reg : registrationsList) {
            responses.add(mapToResponse(reg));
        }
        return responses;
    }

    private void checkIdentifierCollision(BusinessIdentifierType type, String value) {
        if (type == null || value == null || value.isBlank()) {
            return;
        }

        Optional<BusinessIdentity> existingOpt = identityRepository.findByIdentifierTypeAndIdentifierValue(type, value);
        if (existingOpt.isPresent()) {
            BusinessIdentity existing = existingOpt.get();
            if (ACTIVE_OR_RESERVED.contains(existing.getStatus())) {
                Long conflictingId = null;
                if (existing.getRegistration() != null) {
                    conflictingId = existing.getRegistration().getId();
                } else if (existing.getTenant() != null) {
                    conflictingId = existing.getTenant().getId();
                }

                log.warn("BUSINESS_IDENTITY_COLLISION: {} '{}' is already {} by entityId={}",
                        type, value, existing.getStatus(), conflictingId);

                throw new BusinessIdentityCollisionException(
                        type.name(), value, existing.getStatus().name(), conflictingId);
            }
        }
    }

    private void validateSuperAdmin(User user) {
        if (user == null || user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only Super Admin can initiate, verify, or reject pharmacy registrations");
        }
        if (!user.isActive() || user.isAccountLocked()) {
            throw new AccessDeniedException("Admin account is disabled or locked");
        }
    }

    private PharmacyRegistrationResponse mapToResponse(PharmacyRegistration reg) {
        List<BusinessIdentityDto> identityDtos = new ArrayList<>();
        if (reg.getBusinessIdentities() != null) {
            for (BusinessIdentity bi : reg.getBusinessIdentities()) {
                identityDtos.add(BusinessIdentityDto.builder()
                        .id(bi.getId())
                        .identifierType(bi.getIdentifierType().name())
                        .identifierValue(bi.getIdentifierValue())
                        .entityType(bi.getEntityType().name())
                        .entityId(reg.getId())
                        .status(bi.getStatus().name())
                        .verifiedAt(bi.getVerifiedAt())
                        .build());
            }
        }

        Long verifiedByUserId = null;
        if (reg.getVerifiedBy() != null) {
            verifiedByUserId = reg.getVerifiedBy().getId();
        }

        return PharmacyRegistrationResponse.builder()
                .id(reg.getId())
                .legalBusinessName(reg.getLegalBusinessName())
                .tradeName(reg.getTradeName())
                .proposedSlug(reg.getProposedSlug())
                .status(reg.getStatus().name())
                .proposedOwnerEmail(reg.getProposedOwnerEmail())
                .proposedOwnerName(reg.getProposedOwnerName())
                .proposedOwnerPhone(reg.getProposedOwnerPhone())
                .addressLine1(reg.getAddressLine1())
                .city(reg.getCity())
                .state(reg.getState())
                .postalCode(reg.getPostalCode())
                .verificationNotes(reg.getVerificationNotes())
                .verifiedByUserId(verifiedByUserId)
                .verifiedAt(reg.getVerifiedAt())
                .rejectionReason(reg.getRejectionReason())
                .businessIdentities(identityDtos)
                .createdAt(reg.getCreatedAt())
                .updatedAt(reg.getUpdatedAt())
                .build();
    }
}
