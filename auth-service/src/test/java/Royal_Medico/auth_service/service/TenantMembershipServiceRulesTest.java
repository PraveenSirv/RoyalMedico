package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.repository.TenantMembershipRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import Royal_Medico.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantMembershipServiceRulesTest {

    @Mock
    private TenantMembershipRepository tenantMembershipRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TenantMembershipService tenantMembershipService;

    private User ownerUser;
    private User staffUser;
    private Tenant tenantA;
    private Tenant tenantB;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setEmail("owner@pharmacy.com");
        ownerUser.setName("Rajesh Owner");
        ownerUser.setRole(Role.CUSTOMER);
        ownerUser.setActive(true);

        staffUser = new User();
        staffUser.setId(2L);
        staffUser.setEmail("staff@pharmacy.com");
        staffUser.setName("Suresh Staff");
        staffUser.setRole(Role.CUSTOMER);
        staffUser.setActive(true);

        tenantA = new Tenant("Apollo Pharmacy", "apollo-pharmacy");
        tenantA.setId(10L);
        tenantA.setStatus(TenantStatus.ACTIVE);

        tenantB = new Tenant("MedPlus Pharmacy", "medplus-pharmacy");
        tenantB.setId(20L);
        tenantB.setStatus(TenantStatus.ACTIVE);
    }

    @Test
    @DisplayName("Owner can belong to multiple pharmacy tenants")
    void ownerCanBelongToMultipleTenants() {
        // Given owner joining Tenant A
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(ownerUser));
        when(tenantRepository.findById(10L)).thenReturn(Optional.of(tenantA));
        when(tenantMembershipRepository.findByUserIdAndTenantId(1L, 10L)).thenReturn(Optional.empty());
        when(tenantMembershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> {
            TenantMembership m = invocation.getArgument(0);
            m.setId(101L);
            return m;
        });

        TenantMembership membershipA = tenantMembershipService.createMembership(1L, 10L, TenantRole.OWNER);
        assertNotNull(membershipA);
        assertEquals(TenantRole.OWNER, membershipA.getRole());
        assertEquals(10L, membershipA.getTenant().getId());

        // Given owner also joining Tenant B
        when(tenantRepository.findById(20L)).thenReturn(Optional.of(tenantB));
        when(tenantMembershipRepository.findByUserIdAndTenantId(1L, 20L)).thenReturn(Optional.empty());
        when(tenantMembershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> {
            TenantMembership m = invocation.getArgument(0);
            m.setId(102L);
            return m;
        });

        TenantMembership membershipB = tenantMembershipService.createMembership(1L, 20L, TenantRole.OWNER);
        assertNotNull(membershipB);
        assertEquals(TenantRole.OWNER, membershipB.getRole());
        assertEquals(20L, membershipB.getTenant().getId());

        // Verify neither attempt checked the staff single-tenant restriction
        verify(tenantMembershipRepository, never())
                .findByUserIdAndRoleAndStatus(eq(1L), eq(TenantRole.PHARMACY_STAFF), eq(MembershipStatus.ACTIVE));
    }

    @Test
    @DisplayName("Staff cannot belong to a second pharmacy tenant")
    void staffCannotBelongToSecondTenant() {
        // Given staff member already has an active staff membership in Tenant A
        TenantMembership existingStaffMembership = new TenantMembership(staffUser, tenantA, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE);
        existingStaffMembership.setId(201L);

        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(staffUser));
        when(tenantRepository.findById(20L)).thenReturn(Optional.of(tenantB));
        when(tenantMembershipRepository.findByUserIdAndRoleAndStatus(2L, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(existingStaffMembership));

        // When attempting to add staff to Tenant B
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                tenantMembershipService.createMembership(2L, 20L, TenantRole.PHARMACY_STAFF)
        );

        assertTrue(ex.getMessage().contains("Pharmacy staff can only belong to one pharmacy tenant"));
        assertTrue(ex.getMessage().contains("Apollo Pharmacy"));
        verify(tenantMembershipRepository, never()).save(any(TenantMembership.class));
    }

    @Test
    @DisplayName("Staff can have membership in their single tenant (foundation for intra-tenant branch assignments)")
    void staffCanBelongToSingleTenantForBranchScoping() {
        // Staff joining Tenant A successfully
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(staffUser));
        when(tenantRepository.findById(10L)).thenReturn(Optional.of(tenantA));
        when(tenantMembershipRepository.findByUserIdAndRoleAndStatus(2L, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(tenantMembershipRepository.findByUserIdAndTenantId(2L, 10L)).thenReturn(Optional.empty());
        when(tenantMembershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> {
            TenantMembership m = invocation.getArgument(0);
            m.setId(201L);
            return m;
        });

        TenantMembership membership = tenantMembershipService.createMembership(2L, 10L, TenantRole.PHARMACY_STAFF);
        assertNotNull(membership);
        assertEquals(TenantRole.PHARMACY_STAFF, membership.getRole());
        assertEquals(10L, membership.getTenant().getId());
        assertEquals(MembershipStatus.ACTIVE, membership.getStatus());

        // Verifies the user is strictly anchored to Tenant A, establishing the tenant boundary for intra-tenant branches
        verify(tenantMembershipRepository).save(any(TenantMembership.class));
    }

    @Test
    @DisplayName("Inactive staff membership does not prevent joining another tenant")
    void inactiveStaffMembership_allowsJoiningAnotherTenant() {
        // Given past staff membership in Tenant A is INACTIVE (e.g. employee resigned)
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(staffUser));
        when(tenantRepository.findById(20L)).thenReturn(Optional.of(tenantB));
        // No ACTIVE staff membership across any tenant
        when(tenantMembershipRepository.findByUserIdAndRoleAndStatus(2L, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(tenantMembershipRepository.findByUserIdAndTenantId(2L, 20L)).thenReturn(Optional.empty());
        when(tenantMembershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> {
            TenantMembership m = invocation.getArgument(0);
            m.setId(202L);
            return m;
        });

        // When staff joins Tenant B, it succeeds
        TenantMembership membershipB = tenantMembershipService.createMembership(2L, 20L, TenantRole.PHARMACY_STAFF);
        assertNotNull(membershipB);
        assertEquals(20L, membershipB.getTenant().getId());
        assertEquals(TenantRole.PHARMACY_STAFF, membershipB.getRole());
        assertEquals(MembershipStatus.ACTIVE, membershipB.getStatus());
    }

    @Test
    @DisplayName("Reactivating inactive staff membership fails if user is already active in another tenant")
    void reactivatingStaffMembership_failsWhenActiveInAnotherTenant() {
        TenantMembership pastMembershipA = new TenantMembership(staffUser, tenantA, TenantRole.PHARMACY_STAFF, MembershipStatus.INACTIVE);
        pastMembershipA.setId(201L);

        TenantMembership currentMembershipB = new TenantMembership(staffUser, tenantB, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE);
        currentMembershipB.setId(202L);

        when(tenantMembershipRepository.findById(201L)).thenReturn(Optional.of(pastMembershipA));
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(staffUser));
        when(tenantMembershipRepository.findByUserIdAndRoleAndStatus(2L, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(currentMembershipB));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                tenantMembershipService.updateMembershipStatus(201L, MembershipStatus.ACTIVE)
        );

        assertTrue(ex.getMessage().contains("Cannot activate staff membership"));
        assertTrue(ex.getMessage().contains("MedPlus Pharmacy"));
        verify(tenantMembershipRepository, never()).save(any(TenantMembership.class));
    }

    @Test
    @DisplayName("Concurrent creation race condition is caught by database constraint violation translation")
    void concurrentCreation_caughtByDatabaseConstraintViolation() {
        // Simulate race condition where application check passes concurrently, but database partial unique index rejects
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(staffUser));
        when(tenantRepository.findById(20L)).thenReturn(Optional.of(tenantB));
        when(tenantMembershipRepository.findByUserIdAndRoleAndStatus(2L, TenantRole.PHARMACY_STAFF, MembershipStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(tenantMembershipRepository.findByUserIdAndTenantId(2L, 20L)).thenReturn(Optional.empty());
        when(tenantMembershipRepository.save(any(TenantMembership.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint uk_user_active_staff_membership"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                tenantMembershipService.createMembership(2L, 20L, TenantRole.PHARMACY_STAFF)
        );

        assertTrue(ex.getMessage().contains("Pharmacy staff can only belong to one pharmacy tenant"));
    }
}

