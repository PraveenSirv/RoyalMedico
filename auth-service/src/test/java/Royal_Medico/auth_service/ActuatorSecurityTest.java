package Royal_Medico.auth_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Health endpoint is accessible without authentication")
    void healthEndpoint_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Unapproved actuator endpoint /actuator/env is not accessible")
    void envEndpoint_isNotAccessible() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertTrue(statusCode == 401 || statusCode == 403 || statusCode == 404,
                            "Expected 401, 403, or 404 for unapproved actuator endpoint, but got: " + statusCode);
                });
    }

    @Test
    @DisplayName("Unapproved actuator endpoint /actuator/beans is not accessible")
    void beansEndpoint_isNotAccessible() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertTrue(statusCode == 401 || statusCode == 403 || statusCode == 404,
                            "Expected 401, 403, or 404 for unapproved actuator endpoint, but got: " + statusCode);
                });
    }
}

