package royal_medico.auth_service.entity;

import java.util.List;
import java.util.Arrays;

public enum Role {
    ADMIN(Arrays.asList("MEDICINE_READ", "MEDICINE_WRITE", "ORDER_CREATE", "INVENTORY_UPDATE", "BILLING_MANAGE")),
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
