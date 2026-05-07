package royal_medico.auth_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.base64-secret:NTQ2NzU2NkI1OTcwMzM3MzM2NzYzOTc5MjQ0MjI2NDUyOTQ4NDA0RDYzNTE2NjU0NkE1NzZFNUE3MjM0NzUzNw==}")
    private String base64Secret;

    @Value("${jwt.issuer:royal-medico-auth-service}")
    private String issuer;

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

    private Key getSigningKey() {
        byte[] secretKeyBytes = java.util.Base64.getDecoder().decode(base64Secret);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

    public String generateAccessToken(Long userId, String email, String role, java.util.List<String> authorities) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .setIssuer(issuer)
                .setAudience("royal-medico-gateway")
                .claim("userId", userId.toString())
                .claim("role", role)
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
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

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
