package Royal_Medico.auth_service.exception;

import lombok.Getter;

@Getter
public class BusinessIdentityCollisionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String identifierType;
    private final String identifierValue;
    private final String existingStatus;
    private final Long conflictingEntityId;

    public BusinessIdentityCollisionException(String message) {
        super(message);
        this.identifierType = null;
        this.identifierValue = null;
        this.existingStatus = null;
        this.conflictingEntityId = null;
    }

    public BusinessIdentityCollisionException(String identifierType, String identifierValue, String existingStatus, Long conflictingEntityId) {
        super(buildMessage(identifierType, identifierValue, existingStatus, conflictingEntityId));
        this.identifierType = identifierType;
        this.identifierValue = identifierValue;
        this.existingStatus = existingStatus != null ? existingStatus : "RESERVED";
        this.conflictingEntityId = conflictingEntityId;
    }

    public BusinessIdentityCollisionException(String identifierType, String identifierValue, String existingStatus) {
        this(identifierType, identifierValue, existingStatus, null);
    }

    private static String buildMessage(String identifierType, String identifierValue, String existingStatus, Long conflictingEntityId) {
        if ("PROPOSED_SLUG".equalsIgnoreCase(identifierType)) {
            return String.format("Pharmacy slug collision: Proposed slug '%s' is already claimed or in use", identifierValue);
        }
        String status = existingStatus != null ? existingStatus : "RESERVED";
        if (conflictingEntityId != null) {
            String entityDesc = "RESERVED".equalsIgnoreCase(status) ? "registration" : "tenant";
            return String.format("Statutory identifier collision: %s '%s' is already %s by %s ID %d",
                    identifierType, identifierValue, status, entityDesc, conflictingEntityId);
        } else {
            return String.format("Statutory identifier collision: %s '%s' is already %s",
                    identifierType, identifierValue, status);
        }
    }

    public String getIdentifierType() { return identifierType; }
    public String getIdentifierValue() { return identifierValue; }
    public String getExistingStatus() { return existingStatus; }
    public Long getConflictingEntityId() { return conflictingEntityId; }
}
