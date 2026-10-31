package Royal_Medico.auth_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PharmacyRegistrationResponse {

    private Long id;
    private String legalBusinessName;
    private String tradeName;
    private String proposedSlug;
    private String status;
    private String proposedOwnerEmail;
    private String proposedOwnerName;
    private String proposedOwnerPhone;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String verificationNotes;
    private Long verifiedByUserId;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    private List<BusinessIdentityDto> businessIdentities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PharmacyRegistrationResponse() {}

    public PharmacyRegistrationResponse(
            Long id, String legalBusinessName, String tradeName, String proposedSlug,
            String status, String proposedOwnerEmail, String proposedOwnerName,
            String proposedOwnerPhone, String addressLine1, String city, String state,
            String postalCode, String verificationNotes, Long verifiedByUserId,
            LocalDateTime verifiedAt, String rejectionReason,
            List<BusinessIdentityDto> businessIdentities, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.legalBusinessName = legalBusinessName;
        this.tradeName = tradeName;
        this.proposedSlug = proposedSlug;
        this.status = status;
        this.proposedOwnerEmail = proposedOwnerEmail;
        this.proposedOwnerName = proposedOwnerName;
        this.proposedOwnerPhone = proposedOwnerPhone;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.verificationNotes = verificationNotes;
        this.verifiedByUserId = verifiedByUserId;
        this.verifiedAt = verifiedAt;
        this.rejectionReason = rejectionReason;
        this.businessIdentities = businessIdentities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLegalBusinessName() { return legalBusinessName; }
    public void setLegalBusinessName(String legalBusinessName) { this.legalBusinessName = legalBusinessName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getProposedSlug() { return proposedSlug; }
    public void setProposedSlug(String proposedSlug) { this.proposedSlug = proposedSlug; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProposedOwnerEmail() { return proposedOwnerEmail; }
    public void setProposedOwnerEmail(String proposedOwnerEmail) { this.proposedOwnerEmail = proposedOwnerEmail; }
    public String getProposedOwnerName() { return proposedOwnerName; }
    public void setProposedOwnerName(String proposedOwnerName) { this.proposedOwnerName = proposedOwnerName; }
    public String getProposedOwnerPhone() { return proposedOwnerPhone; }
    public void setProposedOwnerPhone(String proposedOwnerPhone) { this.proposedOwnerPhone = proposedOwnerPhone; }
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
    public Long getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(Long verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public List<BusinessIdentityDto> getBusinessIdentities() { return businessIdentities; }
    public void setBusinessIdentities(List<BusinessIdentityDto> businessIdentities) { this.businessIdentities = businessIdentities; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PharmacyRegistrationResponseBuilder builder() {
        return new PharmacyRegistrationResponseBuilder();
    }

    public static class PharmacyRegistrationResponseBuilder {
        private Long id;
        private String legalBusinessName;
        private String tradeName;
        private String proposedSlug;
        private String status;
        private String proposedOwnerEmail;
        private String proposedOwnerName;
        private String proposedOwnerPhone;
        private String addressLine1;
        private String city;
        private String state;
        private String postalCode;
        private String verificationNotes;
        private Long verifiedByUserId;
        private LocalDateTime verifiedAt;
        private String rejectionReason;
        private List<BusinessIdentityDto> businessIdentities;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PharmacyRegistrationResponseBuilder id(Long id) { this.id = id; return this; }
        public PharmacyRegistrationResponseBuilder legalBusinessName(String legalBusinessName) { this.legalBusinessName = legalBusinessName; return this; }
        public PharmacyRegistrationResponseBuilder tradeName(String tradeName) { this.tradeName = tradeName; return this; }
        public PharmacyRegistrationResponseBuilder proposedSlug(String proposedSlug) { this.proposedSlug = proposedSlug; return this; }
        public PharmacyRegistrationResponseBuilder status(String status) { this.status = status; return this; }
        public PharmacyRegistrationResponseBuilder proposedOwnerEmail(String proposedOwnerEmail) { this.proposedOwnerEmail = proposedOwnerEmail; return this; }
        public PharmacyRegistrationResponseBuilder proposedOwnerName(String proposedOwnerName) { this.proposedOwnerName = proposedOwnerName; return this; }
        public PharmacyRegistrationResponseBuilder proposedOwnerPhone(String proposedOwnerPhone) { this.proposedOwnerPhone = proposedOwnerPhone; return this; }
        public PharmacyRegistrationResponseBuilder addressLine1(String addressLine1) { this.addressLine1 = addressLine1; return this; }
        public PharmacyRegistrationResponseBuilder city(String city) { this.city = city; return this; }
        public PharmacyRegistrationResponseBuilder state(String state) { this.state = state; return this; }
        public PharmacyRegistrationResponseBuilder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public PharmacyRegistrationResponseBuilder verificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; return this; }
        public PharmacyRegistrationResponseBuilder verifiedByUserId(Long verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; return this; }
        public PharmacyRegistrationResponseBuilder verifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; return this; }
        public PharmacyRegistrationResponseBuilder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public PharmacyRegistrationResponseBuilder businessIdentities(List<BusinessIdentityDto> businessIdentities) { this.businessIdentities = businessIdentities; return this; }
        public PharmacyRegistrationResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PharmacyRegistrationResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PharmacyRegistrationResponse build() {
            return new PharmacyRegistrationResponse(
                    id, legalBusinessName, tradeName, proposedSlug, status,
                    proposedOwnerEmail, proposedOwnerName, proposedOwnerPhone,
                    addressLine1, city, state, postalCode, verificationNotes,
                    verifiedByUserId, verifiedAt, rejectionReason, businessIdentities,
                    createdAt, updatedAt
            );
        }
    }
}
