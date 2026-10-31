package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.repository.TenantMembershipRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import Royal_Medico.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantMembershipService {

    private final TenantMembershipRepository tenantMembershipRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    /**
     * Creates or reactivates a tenant membership with strict role-based tenancy validation.
     * 
     * Business Invariants:
     * 1. A Pharmacy Owner can have active memberships across multiple pharmacy tenants.
     * 2. Pharmacy Staff can belong to only ONE pharmacy tenant with ACTIVE status.
     * 3. Inactive or suspended staff memberships do not prevent joining a new tenant.
     * 4. Concurrency is guarded at both the transaction level (pessimistic user lock)
     *    and the database engine level (partial unique index uk_user_active_staff_membership).
     */
    @Transactional
    public TenantMembership createMembership(Long userId, Long tenantId, TenantRole role) {
        if (userId == null || tenantId == null || role == null) {
            throw new IllegalArgumentException("userId, tenantId, and role must not be null");
        }

        // 1. Acquire pessimistic write lock on the User to serialize concurrent membership attempts
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + tenantId));

        if (tenant.getStatus() == TenantStatus.CLOSED) {
            throw new IllegalStateException("Cannot create membership in a CLOSED tenant");
        }

        // 2. Enforce Staff Single-Tenant Invariant
        if (role == TenantRole.PHARMACY_STAFF) {
            Optional<TenantMembership> activeStaffOpt = tenantMembershipRepository
                    .findByUserIdAndRoleAndStatus(user.getId(), TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE);

            if (activeStaffOpt.isPresent()) {
                TenantMembership activeStaff = activeStaffOpt.get();
                if (!activeStaff.getTenant().getId().equals(tenantId)) {
                    log.warn("MEMBERSHIP_AUDIT_STAFF_RESTRICTION_BLOCKED: User {} already has active staff membership in tenant {} ({})",
                            user.getId(), activeStaff.getTenant().getId(), activeStaff.getTenant().getName());
                    throw new IllegalStateException(
                            "Pharmacy staff can only belong to one pharmacy tenant. User is already an active staff member in tenant: "
                                    + activeStaff.getTenant().getName()
                    );
                }
            }
        }

        // 3. Inspect existing membership in this specific tenant
        Optional<TenantMembership> existingInTenantOpt = tenantMembershipRepository
                .findByUserIdAndTenantId(user.getId(), tenant.getId());

        if (existingInTenantOpt.isPresent()) {
            TenantMembership existing = existingInTenantOpt.get();
            if (existing.getStatus() == MembershipStatus.ACTIVE) {
                log.warn("MEMBERSHIP_AUDIT_DUPLICATE: User {} already has an active membership in tenant {}",
                        user.getId(), tenant.getId());
                throw new IllegalStateException("User already has an active membership in tenant: " + tenant.getName());
            }

            // Reactivate previously inactive/suspended membership
            existing.setRole(role);
            existing.setStatus(MembershipStatus.ACTIVE);
            TenantMembership reactivated = tenantMembershipRepository.save(existing);
            log.info("MEMBERSHIP_AUDIT_REACTIVATED: Reactivated membership {} for user {} in tenant {} with role {}",
                    reactivated.getId(), user.getId(), tenant.getId(), role);
            return reactivated;
        }

        // 4. Create new membership
        try {
            TenantMembership newMembership = new TenantMembership(user, tenant, role, MembershipStatus.ACTIVE);
            TenantMembership saved = tenantMembershipRepository.save(newMembership);
            log.info("MEMBERSHIP_AUDIT_CREATED: Created membership {} for user {} in tenant {} with role {}",
                    saved.getId(), user.getId(), tenant.getId(), role);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.error("MEMBERSHIP_AUDIT_DB_VIOLATION: Database constraint collision for user {} in tenant {}",
                    userId, tenantId, ex);
            if (role == TenantRole.PHARMACY_STAFF) {
                throw new IllegalStateException("Pharmacy staff can only belong to one pharmacy tenant.", ex);
            }
            throw new IllegalStateException("Duplicate or invalid membership", ex);
        }
    }

    /**
     * Updates membership status with validation against single-tenant staff invariant.
     */
    @Transactional
    public TenantMembership updateMembershipStatus(Long membershipId, MembershipStatus newStatus) {
        TenantMembership membership = tenantMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found with id: " + membershipId));

        if (newStatus == MembershipStatus.ACTIVE && membership.getRole() == TenantRole.PHARMACY_STAFF) {
            // Lock user to prevent concurrent status reactivation
            userRepository.findByIdWithLock(membership.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Optional<TenantMembership> existingActiveStaff = tenantMembershipRepository
                    .findByUserIdAndRoleAndStatus(membership.getUser().getId(), TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE);

            if (existingActiveStaff.isPresent() && !existingActiveStaff.get().getId().equals(membershipId)) {
                TenantMembership activeOther = existingActiveStaff.get();
                log.warn("MEMBERSHIP_AUDIT_REACTIVATION_BLOCKED: Cannot reactivate staff membership for user {} in tenant {} because active in tenant {}",
                        membership.getUser().getId(), membership.getTenant().getId(), activeOther.getTenant().getId());
                throw new IllegalStateException(
                        "Cannot activate staff membership. User already has active staff membership in tenant: "
                                + activeOther.getTenant().getName()
                );
            }
        }

        membership.setStatus(newStatus);
        TenantMembership updated = tenantMembershipRepository.save(membership);
        log.info("MEMBERSHIP_AUDIT_STATUS_UPDATED: Membership {} status updated to {}", membershipId, newStatus);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<TenantMembership> getActiveMemberships(Long userId) {
        return tenantMembershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Optional<TenantMembership> getMembership(Long userId, Long tenantId) {
        return tenantMembershipRepository.findByUserIdAndTenantId(userId, tenantId);
    }
}

