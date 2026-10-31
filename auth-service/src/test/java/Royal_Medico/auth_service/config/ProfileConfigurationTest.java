package Royal_Medico.auth_service.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileConfigurationTest {

    @Test
    @DisplayName("application.yml does not contain hardcoded JWT secret or DB password")
    void applicationYml_hasNoHardcodedSecrets() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/application.yml")) {
            assertNotNull(in, "application.yml must exist");
            Map<String, Object> data = yaml.load(in);

            Map<?, ?> spring = (Map<?, ?>) data.get("spring");
            if (spring != null && spring.containsKey("datasource")) {
                Map<?, ?> datasource = (Map<?, ?>) spring.get("datasource");
                assertFalse(datasource.containsKey("password"), "Base application.yml must not hardcode datasource password");
            }

            Map<?, ?> jwt = (Map<?, ?>) data.get("jwt");
            if (jwt != null) {
                assertFalse(jwt.containsKey("base64-secret"), "Base application.yml must not hardcode jwt.base64-secret");
            }
        } catch (Exception e) {
            fail("Failed to inspect application.yml: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("application-dev.yml uses environment placeholders and approved local defaults")
    void applicationDevYml_usesApprovedPlaceholders() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/application-dev.yml")) {
            assertNotNull(in, "application-dev.yml must exist");
            Map<String, Object> data = yaml.load(in);

            Map<?, ?> spring = (Map<?, ?>) data.get("spring");
            assertNotNull(spring);
            Map<?, ?> datasource = (Map<?, ?>) spring.get("datasource");
            assertNotNull(datasource);

            String url = (String) datasource.get("url");
            String username = (String) datasource.get("username");
            String password = (String) datasource.get("password");

            assertTrue(url.contains("${SPRING_DATASOURCE_URL:"), "Dev URL must allow environment override");
            assertTrue(url.contains("localhost:5432/auth_user"), "Dev URL must permit local default");
            assertTrue(username.contains("postgres"), "Dev username must permit local default");
            assertTrue(password.contains("${SPRING_DATASOURCE_PASSWORD") || password.contains("${DB_PASSWORD"),
                    "Dev password must come from environment variable");
            assertNotEquals("root", password, "Dev password must not be hardcoded literal");

            Map<?, ?> jwt = (Map<?, ?>) data.get("jwt");
            assertNotNull(jwt);
            String secret = (String) jwt.get("base64-secret");
            assertTrue(secret.contains("${JWT_BASE64_SECRET}"), "Dev JWT secret must come from environment variable");
        } catch (Exception e) {
            fail("Failed to inspect application-dev.yml: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("application-prod.yml requires strict environment variables and sets ddl-auto to validate")
    void applicationProdYml_requiresStrictEnvAndValidate() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/application-prod.yml")) {
            assertNotNull(in, "application-prod.yml must exist");
            Map<String, Object> data = yaml.load(in);

            Map<?, ?> spring = (Map<?, ?>) data.get("spring");
            assertNotNull(spring);
            Map<?, ?> datasource = (Map<?, ?>) spring.get("datasource");
            assertNotNull(datasource);

            assertEquals("${SPRING_DATASOURCE_URL}", datasource.get("url"));
            assertEquals("${SPRING_DATASOURCE_USERNAME}", datasource.get("username"));
            assertEquals("${SPRING_DATASOURCE_PASSWORD}", datasource.get("password"));

            Map<?, ?> jpa = (Map<?, ?>) spring.get("jpa");
            assertNotNull(jpa);
            Map<?, ?> hibernate = (Map<?, ?>) jpa.get("hibernate");
            assertNotNull(hibernate);
            assertEquals("validate", hibernate.get("ddl-auto"), "Production must use ddl-auto: validate");
            assertEquals(false, jpa.get("show-sql"), "Production must disable show-sql");

            Map<?, ?> jwt = (Map<?, ?>) data.get("jwt");
            assertNotNull(jwt);
            assertEquals("${JWT_BASE64_SECRET}", jwt.get("base64-secret"), "Production must require JWT_BASE64_SECRET without default");

            Map<?, ?> management = (Map<?, ?>) data.get("management");
            assertNotNull(management);
            Map<?, ?> endpoint = (Map<?, ?>) management.get("endpoint");
            assertNotNull(endpoint);
            Map<?, ?> health = (Map<?, ?>) endpoint.get("health");
            assertNotNull(health);
            assertEquals("never", health.get("show-details"), "Production must suppress health details");
        } catch (Exception e) {
            fail("Failed to inspect application-prod.yml: " + e.getMessage());
        }
    }
}

