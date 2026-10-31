package Royal_Medico.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PharmacyRegistrationRequest {

    @NotBlank(message = "Legal business name is required")
    @Size(max = 255, message = "Legal business name cannot exceed 255 characters")
    private String legalBusinessName;

    @NotBlank(message = "Trade name is required")
    @Size(max = 255, message = "Trade name cannot exceed 255 characters")
    private String tradeName;

    @NotBlank(message = "Proposed slug is required")
    @Size(max = 100, message = "Proposed slug cannot exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    private String proposedSlug;

    @NotBlank(message = "Proposed owner email is required")
    @Email(message = "Invalid email format")
    private String proposedOwnerEmail;

    @NotBlank(message = "Proposed owner name is required")
    private String proposedOwnerName;

    private String proposedOwnerPhone;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state = "Maharashtra";

    @NotBlank(message = "Postal PIN code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian postal PIN code")
    private String postalCode;

    @NotBlank(message = "GSTIN is mandatory for pharmacy registration")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid Indian GSTIN structural format")
    private String gstin;

    @NotBlank(message = "Business PAN is mandatory")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", 
             message = "Invalid Indian PAN structural format")
    private String pan;

    @NotBlank(message = "Drug licence Form 20 is mandatory")
    @Pattern(regexp = "^[A-Z0-9/_-]{5,50}$", message = "Invalid Drug Licence Form 20 format")
    private String drugLicenseForm20;

    @Pattern(regexp = "^$|^[A-Z0-9/_-]{5,50}$", message = "Invalid Drug Licence Form 21 format")
    private String drugLicenseForm21;

    @Pattern(regexp = "^$|^[A-Z0-9/_-]{5,50}$", message = "Invalid Drug Licence Form 20B format")
    private String drugLicenseForm20B;

    @Pattern(regexp = "^$|^[A-Z0-9/_-]{5,50}$", message = "Invalid Drug Licence Form 21B format")
    private String drugLicenseForm21B;

    @Pattern(regexp = "^$|^[A-Z0-9/_-]{5,50}$", message = "Invalid Drug Licence Form 20C format")
    private String drugLicenseForm20C;

    @Pattern(regexp = "^$|^[0-9]{14}$", message = "Invalid 14-digit FSSAI licence number")
    private String fssai;

    public PharmacyRegistrationRequest() {}

    public PharmacyRegistrationRequest(
            String legalBusinessName, String tradeName, String proposedSlug,
            String proposedOwnerEmail, String proposedOwnerName, String proposedOwnerPhone,
            String addressLine1, String city, String state, String postalCode,
            String gstin, String pan, String drugLicenseForm20, String drugLicenseForm21,
            String drugLicenseForm20B, String drugLicenseForm21B, String drugLicenseForm20C, String fssai) {
        this.legalBusinessName = legalBusinessName;
        this.tradeName = tradeName;
        this.proposedSlug = proposedSlug;
        this.proposedOwnerEmail = proposedOwnerEmail;
        this.proposedOwnerName = proposedOwnerName;
        this.proposedOwnerPhone = proposedOwnerPhone;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.gstin = gstin;
        this.pan = pan;
        this.drugLicenseForm20 = drugLicenseForm20;
        this.drugLicenseForm21 = drugLicenseForm21;
        this.drugLicenseForm20B = drugLicenseForm20B;
        this.drugLicenseForm21B = drugLicenseForm21B;
        this.drugLicenseForm20C = drugLicenseForm20C;
        this.fssai = fssai;
    }

    public String getLegalBusinessName() { return legalBusinessName; }
    public void setLegalBusinessName(String legalBusinessName) { this.legalBusinessName = legalBusinessName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getProposedSlug() { return proposedSlug; }
    public void setProposedSlug(String proposedSlug) { this.proposedSlug = proposedSlug; }
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
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
    public String getDrugLicenseForm20() { return drugLicenseForm20; }
    public void setDrugLicenseForm20(String drugLicenseForm20) { this.drugLicenseForm20 = drugLicenseForm20; }
    public String getDrugLicenseForm21() { return drugLicenseForm21; }
    public void setDrugLicenseForm21(String drugLicenseForm21) { this.drugLicenseForm21 = drugLicenseForm21; }
    public String getDrugLicenseForm20B() { return drugLicenseForm20B; }
    public void setDrugLicenseForm20B(String drugLicenseForm20B) { this.drugLicenseForm20B = drugLicenseForm20B; }
    public String getDrugLicenseForm21B() { return drugLicenseForm21B; }
    public void setDrugLicenseForm21B(String drugLicenseForm21B) { this.drugLicenseForm21B = drugLicenseForm21B; }
    public String getDrugLicenseForm20C() { return drugLicenseForm20C; }
    public void setDrugLicenseForm20C(String drugLicenseForm20C) { this.drugLicenseForm20C = drugLicenseForm20C; }
    public String getFssai() { return fssai; }
    public void setFssai(String fssai) { this.fssai = fssai; }

    public static PharmacyRegistrationRequestBuilder builder() {
        return new PharmacyRegistrationRequestBuilder();
    }

    public static class PharmacyRegistrationRequestBuilder {
        private String legalBusinessName;
        private String tradeName;
        private String proposedSlug;
        private String proposedOwnerEmail;
        private String proposedOwnerName;
        private String proposedOwnerPhone;
        private String addressLine1;
        private String city;
        private String state = "Maharashtra";
        private String postalCode;
        private String gstin;
        private String pan;
        private String drugLicenseForm20;
        private String drugLicenseForm21;
        private String drugLicenseForm20B;
        private String drugLicenseForm21B;
        private String drugLicenseForm20C;
        private String fssai;

        public PharmacyRegistrationRequestBuilder legalBusinessName(String legalBusinessName) { this.legalBusinessName = legalBusinessName; return this; }
        public PharmacyRegistrationRequestBuilder tradeName(String tradeName) { this.tradeName = tradeName; return this; }
        public PharmacyRegistrationRequestBuilder proposedSlug(String proposedSlug) { this.proposedSlug = proposedSlug; return this; }
        public PharmacyRegistrationRequestBuilder proposedOwnerEmail(String proposedOwnerEmail) { this.proposedOwnerEmail = proposedOwnerEmail; return this; }
        public PharmacyRegistrationRequestBuilder proposedOwnerName(String proposedOwnerName) { this.proposedOwnerName = proposedOwnerName; return this; }
        public PharmacyRegistrationRequestBuilder proposedOwnerPhone(String proposedOwnerPhone) { this.proposedOwnerPhone = proposedOwnerPhone; return this; }
        public PharmacyRegistrationRequestBuilder addressLine1(String addressLine1) { this.addressLine1 = addressLine1; return this; }
        public PharmacyRegistrationRequestBuilder city(String city) { this.city = city; return this; }
        public PharmacyRegistrationRequestBuilder state(String state) { this.state = state; return this; }
        public PharmacyRegistrationRequestBuilder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public PharmacyRegistrationRequestBuilder gstin(String gstin) { this.gstin = gstin; return this; }
        public PharmacyRegistrationRequestBuilder pan(String pan) { this.pan = pan; return this; }
        public PharmacyRegistrationRequestBuilder drugLicenseForm20(String drugLicenseForm20) { this.drugLicenseForm20 = drugLicenseForm20; return this; }
        public PharmacyRegistrationRequestBuilder drugLicenseForm21(String drugLicenseForm21) { this.drugLicenseForm21 = drugLicenseForm21; return this; }
        public PharmacyRegistrationRequestBuilder drugLicenseForm20B(String drugLicenseForm20B) { this.drugLicenseForm20B = drugLicenseForm20B; return this; }
        public PharmacyRegistrationRequestBuilder drugLicenseForm21B(String drugLicenseForm21B) { this.drugLicenseForm21B = drugLicenseForm21B; return this; }
        public PharmacyRegistrationRequestBuilder drugLicenseForm20C(String drugLicenseForm20C) { this.drugLicenseForm20C = drugLicenseForm20C; return this; }
        public PharmacyRegistrationRequestBuilder fssai(String fssai) { this.fssai = fssai; return this; }

        public PharmacyRegistrationRequest build() {
            return new PharmacyRegistrationRequest(
                    legalBusinessName, tradeName, proposedSlug, proposedOwnerEmail,
                    proposedOwnerName, proposedOwnerPhone, addressLine1, city, state,
                    postalCode, gstin, pan, drugLicenseForm20, drugLicenseForm21,
                    drugLicenseForm20B, drugLicenseForm21B, drugLicenseForm20C, fssai
            );
        }
    }
}
