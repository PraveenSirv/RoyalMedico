package Royal_Medico.auth_service.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * Strongly typed, validated JWT configuration properties.
 * Reads configuration from 'jwt' prefix (e.g., jwt.base64-secret, jwt.issuer).
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Base64-encoded HMAC-SHA256 signing secret.
     * Must be supplied via environment variable (e.g. JWT_BASE64_SECRET).
     * Must decode to at least 32 bytes (256 bits).
     */
    private String base64Secret;

    /**
     * Token issuer claim (iss).
     */
    private String issuer = "royal-medico-auth-service";

    /**
     * Token audience claim (aud).
     */
    private String audience = "royal-medico-gateway";

    /**
     * Access token lifetime in milliseconds (default: 15 minutes = 900,000 ms).
     */
    private long accessTokenExpirationMs = 15 * 60 * 1000L;

    /**
     * Refresh token validity in days (default: 7 days).
     */
    private long refreshTokenValidityDays = 7;

    public String getBase64Secret() { return base64Secret; }
    public void setBase64Secret(String base64Secret) { this.base64Secret = base64Secret; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public long getAccessTokenExpirationMs() { return accessTokenExpirationMs; }
    public void setAccessTokenExpirationMs(long accessTokenExpirationMs) { this.accessTokenExpirationMs = accessTokenExpirationMs; }
    public long getRefreshTokenValidityDays() { return refreshTokenValidityDays; }
    public void setRefreshTokenValidityDays(long refreshTokenValidityDays) { this.refreshTokenValidityDays = refreshTokenValidityDays; }

    @PostConstruct
    public void validate() {
        if (base64Secret == null || base64Secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT signing secret (jwt.base64-secret) must not be null or blank. Please set the JWT_BASE64_SECRET environment variable.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Secret.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT signing secret (jwt.base64-secret) is not a valid Base64 string", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT signing secret (jwt.base64-secret) must decode to at least 32 bytes (256 bits) for HMAC-SHA algorithms, but found " + keyBytes.length + " bytes");
        }
    }

    /**
     * Returns the validated decoded key bytes for cryptographic key generation.
     */
    public byte[] getDecodedSecretKeyBytes() {
        return Base64.getDecoder().decode(base64Secret.trim());
    }
}

