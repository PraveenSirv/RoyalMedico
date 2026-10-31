package Royal_Medico.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresInMs;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private Long membershipId;
    private String tenantRole;
}

