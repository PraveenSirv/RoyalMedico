package Royal_Medico.auth_service.repository;

import Royal_Medico.auth_service.entity.Tenant;
import Royal_Medico.auth_service.entity.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Optional<Tenant> findByIdAndStatus(Long id, TenantStatus status);
}

