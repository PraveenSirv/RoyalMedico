package Royal_Medico.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMembershipDto {
    private Long membershipId;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private String role;
    private String status;
}

