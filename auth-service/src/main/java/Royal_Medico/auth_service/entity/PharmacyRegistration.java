package Royal_Medico.auth_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pharmacy_registrations")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PharmacyRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_business_name", nullable = false)
    private String legalBusinessName;

    @Column(name = "trade_name", nullable = false)
    private String tradeName;

    @Column(name = "proposed_slug", nullable = false, unique = true, length = 100)
    private String proposedSlug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RegistrationStatus status = RegistrationStatus.PENDING_VERIFICATION;

    @Column(name = "proposed_owner_email", nullable = false)
    private String proposedOwnerEmail;

    @Column(name = "proposed_owner_phone", length = 20)
    private String proposedOwnerPhone;

    @Column(name = "proposed_owner_name", nullable = false)
    private String proposedOwnerName;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 100)
    private String state = "Maharashtra";

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessIdentity> businessIdentities = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    public void addBusinessIdentity(BusinessIdentity identity) {
        businessIdentities.add(identity);
        identity.setRegistration(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLegalBusinessName() { return legalBusinessName; }
    public void setLegalBusinessName(String legalBusinessName) { this.legalBusinessName = legalBusinessName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getProposedSlug() { return proposedSlug; }
    public void setProposedSlug(String proposedSlug) { this.proposedSlug = proposedSlug; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public String getProposedOwnerEmail() { return proposedOwnerEmail; }
    public void setProposedOwnerEmail(String proposedOwnerEmail) { this.proposedOwnerEmail = proposedOwnerEmail; }
    public String getProposedOwnerPhone() { return proposedOwnerPhone; }
    public void setProposedOwnerPhone(String proposedOwnerPhone) { this.proposedOwnerPhone = proposedOwnerPhone; }
    public String getProposedOwnerName() { return proposedOwnerName; }
    public void setProposedOwnerName(String proposedOwnerName) { this.proposedOwnerName = proposedOwnerName; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
    public User getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(User verifiedBy) { this.verifiedBy = verifiedBy; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public List<BusinessIdentity> getBusinessIdentities() { return businessIdentities; }
    public void setBusinessIdentities(List<BusinessIdentity> businessIdentities) { this.businessIdentities = businessIdentities; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
