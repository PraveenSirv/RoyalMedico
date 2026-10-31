package Royal_Medico.auth_service.entity;

import java.util.List;
import java.util.Arrays;

public enum Role {
    SUPER_ADMIN(Arrays.asList("SUPER_ADMIN", "PHARMACY_REGISTER", "PHARMACY_VERIFY", "PHARMACY_GOVERN")),
    PHARMACIST(Arrays.asList("MEDICINE_READ", "MEDICINE_WRITE", "ORDER_CREATE")),
    CUSTOMER(Arrays.asList("MEDICINE_READ", "ORDER_CREATE")),
    DELIVERY_AGENT(Arrays.asList("ORDER_READ", "DELIVERY_MANAGE"));

    private final List<String> authorities;

    Role(List<String> authorities) {
        this.authorities = authorities;
    }

    public List<String> getAuthorities() {
        return authorities;
    }
}
