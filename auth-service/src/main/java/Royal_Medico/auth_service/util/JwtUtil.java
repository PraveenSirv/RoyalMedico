package Royal_Medico.auth_service.util;

import Royal_Medico.auth_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    @Autowired
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getDecodedSecretKeyBytes());
    }

    public String generateAccessToken(Long userId, String email, String role, java.util.List<String> authorities) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .setIssuer(jwtProperties.getIssuer())
                .setAudience(jwtProperties.getAudience())
                .claim("userId", userId.toString())
                .claim("role", role)
                .claim("type", "GLOBAL")
                .claim("tokenType", "GLOBAL")
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpirationMs()))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTenantAccessToken(Long userId, String email, Long tenantId, Long membershipId, String tenantRole, java.util.List<String> authorities) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .setIssuer(jwtProperties.getIssuer())
                .setAudience(jwtProperties.getAudience())
                .claim("userId", userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("membershipId", membershipId.toString())
                .claim("tenantRole", tenantRole)
                .claim("role", tenantRole)
                .claim("type", "TENANT")
                .claim("tokenType", "TENANT")
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpirationMs()))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        String userIdStr = getClaims(token).get("userId", String.class);
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    public Long extractTenantId(String token) {
        String tenantIdStr = getClaims(token).get("tenantId", String.class);
        return tenantIdStr != null ? Long.parseLong(tenantIdStr) : null;
    }

    public Long extractMembershipId(String token) {
        String membershipIdStr = getClaims(token).get("membershipId", String.class);
        return membershipIdStr != null ? Long.parseLong(membershipIdStr) : null;
    }

    public String extractTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpirationMs();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
