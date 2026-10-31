package Royal_Medico.auth_service.util;

import Royal_Medico.auth_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

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

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
