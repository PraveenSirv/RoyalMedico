package Royal_Medico.auth_service.repository;

import Royal_Medico.auth_service.entity.MembershipStatus;
import Royal_Medico.auth_service.entity.TenantMembership;
import Royal_Medico.auth_service.entity.TenantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantMembershipRepository extends JpaRepository<TenantMembership, Long> {
    List<TenantMembership> findByUserId(Long userId);
    List<TenantMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);
    Optional<TenantMembership> findByUserIdAndTenantId(Long userId, Long tenantId);
    Optional<TenantMembership> findByUserIdAndTenantIdAndStatus(Long userId, Long tenantId, MembershipStatus status);
    List<TenantMembership> findByTenantId(Long tenantId);
    Optional<TenantMembership> findByUserIdAndRoleAndStatus(Long userId, TenantRole role, MembershipStatus status);
    boolean existsByUserIdAndRoleAndStatus(Long userId, TenantRole role, MembershipStatus status);
}

