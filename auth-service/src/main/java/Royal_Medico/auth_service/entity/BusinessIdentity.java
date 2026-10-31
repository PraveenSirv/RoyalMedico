package Royal_Medico.auth_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_identities", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_active_business_identity", columnNames = {"identifier_type", "identifier_value", "status"})
       },
       indexes = {
           @Index(name = "idx_business_identities_tenant", columnList = "tenant_id"),
           @Index(name = "idx_business_identities_registration", columnList = "registration_id"),
           @Index(name = "idx_business_identities_status", columnList = "status")
       }
)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BusinessIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type", nullable = false, length = 50)
    private BusinessIdentifierType identifierType;

    @Column(name = "identifier_value", nullable = false, length = 100)
    private String identifierValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private BusinessIdentityEntityType entityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private PharmacyRegistration registration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BusinessIdentityStatus status = BusinessIdentityStatus.RESERVED;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BusinessIdentity() {}

    public BusinessIdentity(BusinessIdentifierType identifierType, String identifierValue, 
                            BusinessIdentityEntityType entityType, PharmacyRegistration registration) {
        this.identifierType = identifierType;
        this.identifierValue = normalize(identifierValue);
        this.entityType = entityType;
        this.registration = registration;
        this.status = BusinessIdentityStatus.RESERVED;
    }

    public static String normalize(String value) {
        return value != null ? value.trim().toUpperCase() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusinessIdentifierType getIdentifierType() { return identifierType; }
    public void setIdentifierType(BusinessIdentifierType identifierType) { this.identifierType = identifierType; }
    public String getIdentifierValue() { return identifierValue; }
    public void setIdentifierValue(String identifierValue) { this.identifierValue = identifierValue; }
    public BusinessIdentityEntityType getEntityType() { return entityType; }
    public void setEntityType(BusinessIdentityEntityType entityType) { this.entityType = entityType; }
    public PharmacyRegistration getRegistration() { return registration; }
    public void setRegistration(PharmacyRegistration registration) { this.registration = registration; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public BusinessIdentityStatus getStatus() { return status; }
    public void setStatus(BusinessIdentityStatus status) { this.status = status; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public User getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(User verifiedBy) { this.verifiedBy = verifiedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
