package Royal_Medico.auth_service.dto;

import java.time.LocalDateTime;

public class BusinessIdentityDto {
    private Long id;
    private String identifierType;
    private String identifierValue;
    private String entityType;
    private Long entityId;
    private String status;
    private LocalDateTime verifiedAt;

    public BusinessIdentityDto() {}

    public BusinessIdentityDto(
            Long id, String identifierType, String identifierValue,
            String entityType, Long entityId, String status, LocalDateTime verifiedAt) {
        this.id = id;
        this.identifierType = identifierType;
        this.identifierValue = identifierValue;
        this.entityType = entityType;
        this.entityId = entityId;
        this.status = status;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentifierType() { return identifierType; }
    public void setIdentifierType(String identifierType) { this.identifierType = identifierType; }
    public String getIdentifierValue() { return identifierValue; }
    public void setIdentifierValue(String identifierValue) { this.identifierValue = identifierValue; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public static BusinessIdentityDtoBuilder builder() {
        return new BusinessIdentityDtoBuilder();
    }

    public static class BusinessIdentityDtoBuilder {
        private Long id;
        private String identifierType;
        private String identifierValue;
        private String entityType;
        private Long entityId;
        private String status;
        private LocalDateTime verifiedAt;

        public BusinessIdentityDtoBuilder id(Long id) { this.id = id; return this; }
        public BusinessIdentityDtoBuilder identifierType(String identifierType) { this.identifierType = identifierType; return this; }
        public BusinessIdentityDtoBuilder identifierValue(String identifierValue) { this.identifierValue = identifierValue; return this; }
        public BusinessIdentityDtoBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public BusinessIdentityDtoBuilder entityId(Long entityId) { this.entityId = entityId; return this; }
        public BusinessIdentityDtoBuilder status(String status) { this.status = status; return this; }
        public BusinessIdentityDtoBuilder verifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; return this; }

        public BusinessIdentityDto build() {
            return new BusinessIdentityDto(id, identifierType, identifierValue, entityType, entityId, status, verifiedAt);
        }
    }
}
