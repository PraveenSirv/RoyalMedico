package Royal_Medico.auth_service.repository;

import Royal_Medico.auth_service.entity.PharmacyRegistration;
import Royal_Medico.auth_service.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRegistrationRepository extends JpaRepository<PharmacyRegistration, Long> {

    Optional<PharmacyRegistration> findByProposedSlug(String proposedSlug);

    boolean existsByProposedSlug(String proposedSlug);

    List<PharmacyRegistration> findByStatus(RegistrationStatus status);

    List<PharmacyRegistration> findByProposedOwnerEmail(String email);
}

