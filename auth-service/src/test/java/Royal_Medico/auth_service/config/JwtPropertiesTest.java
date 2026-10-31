package Royal_Medico.auth_service.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtPropertiesTest {

    @Test
    @DisplayName("Valid 256-bit Base64 secret passes validation")
    void validSecret_passesValidation() {
        JwtProperties properties = new JwtProperties();
        properties.setBase64Secret("NTQ2NzU2NkI1OTcwMzM3MzM2NzYzOTc5MjQ0MjI2NDUyOTQ4NDA0RDYzNTE2NjU0NkE1NzZFNUE3MjM0NzUzNw==");

        assertDoesNotThrow(properties::validate);
        assertNotNull(properties.getDecodedSecretKeyBytes());
        assertTrue(properties.getDecodedSecretKeyBytes().length >= 32);
        assertEquals(64, properties.getDecodedSecretKeyBytes().length);
    }

    @Test
    @DisplayName("Null secret fails validation with IllegalStateException")
    void nullSecret_failsValidation() {
        JwtProperties properties = new JwtProperties();
        properties.setBase64Secret(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("must not be null or blank"));
    }

    @Test
    @DisplayName("Blank secret fails validation with IllegalStateException")
    void blankSecret_failsValidation() {
        JwtProperties properties = new JwtProperties();
        properties.setBase64Secret("   ");

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("must not be null or blank"));
    }

    @Test
    @DisplayName("Invalid Base64 string fails validation with IllegalStateException")
    void invalidBase64_failsValidation() {
        JwtProperties properties = new JwtProperties();
        properties.setBase64Secret("ThisIsNotValidBase64!@#$%^&*()");

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("not a valid Base64 string"));
    }

    @Test
    @DisplayName("Decoded secret shorter than 32 bytes (256 bits) fails validation")
    void shortSecret_failsValidation() {
        JwtProperties properties = new JwtProperties();
        String shortBase64 = Base64.getEncoder().encodeToString("1234567890123456".getBytes());
        properties.setBase64Secret(shortBase64);

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("must decode to at least 32 bytes (256 bits)"));
    }

    @Test
    @DisplayName("Default issuer, audience, and expiration match required specification")
    void defaults_remainConsistent() {
        JwtProperties properties = new JwtProperties();
        assertEquals("royal-medico-auth-service", properties.getIssuer());
        assertEquals("royal-medico-gateway", properties.getAudience());
        assertEquals(15 * 60 * 1000L, properties.getAccessTokenExpirationMs());
        assertEquals(7, properties.getRefreshTokenValidityDays());
    }
}
