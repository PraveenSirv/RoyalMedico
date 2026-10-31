package Royal_Medico.auth_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Unexpected Exception returns sanitized 500 response with errorId")
    void handleGeneralException_sanitizesError() {
        Exception rawDatabaseException = new RuntimeException("FATAL: connection to server at localhost:5432 failed: password authentication failed for user postgres");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneralException(rawDatabaseException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));

        // Must NOT leak database connection details, passwords, or raw exception strings
        String message = (String) body.get("message");
        assertFalse(message.contains("localhost:5432"));
        assertFalse(message.contains("password authentication failed"));
        assertTrue(message.contains("An unexpected error occurred"));

        // Must include unique correlation/errorId
        assertNotNull(body.get("errorId"));
        String errorId = (String) body.get("errorId");
        assertFalse(errorId.isBlank());
        assertTrue(message.contains(errorId));
    }

    @Test
    @DisplayName("BadCredentialsException returns 401 Unauthorized with standard message")
    void handleBadCredentialsException_returns401() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Unauthorized", response.getBody().get("error"));
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    @DisplayName("LockedException returns 401 Unauthorized with lockout message")
    void handleLockedException_returns401() {
        LockedException ex = new LockedException("Account is locked due to too many failed attempts");

        ResponseEntity<Map<String, Object>> response = handler.handleLockedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Unauthorized", response.getBody().get("error"));
        assertEquals("Account is locked due to too many failed attempts", response.getBody().get("message"));
    }

    @Test
    @DisplayName("IllegalArgumentException returns 400 Bad Request with client message")
    void handleIllegalArgumentException_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Email already registered");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Email already registered", response.getBody().get("message"));
    }
}

