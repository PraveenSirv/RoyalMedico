package Royal_Medico.auth_service.repository;

import Royal_Medico.auth_service.entity.BusinessIdentifierType;
import Royal_Medico.auth_service.entity.BusinessIdentity;
import Royal_Medico.auth_service.entity.BusinessIdentityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessIdentityRepository extends JpaRepository<BusinessIdentity, Long> {

    Optional<BusinessIdentity> findByIdentifierTypeAndIdentifierValue(BusinessIdentifierType identifierType, String identifierValue);

    boolean existsByIdentifierTypeAndIdentifierValueAndStatusIn(
            BusinessIdentifierType identifierType, 
            String identifierValue, 
            Collection<BusinessIdentityStatus> statuses
    );

    List<BusinessIdentity> findByRegistrationId(Long registrationId);

    List<BusinessIdentity> findByTenantId(Long tenantId);
}

