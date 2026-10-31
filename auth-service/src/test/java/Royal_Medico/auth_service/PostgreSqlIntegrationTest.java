package Royal_Medico.auth_service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres-it")
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres;

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            postgres = new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("auth_user_test")
                    .withUsername("test_user")
                    .withPassword("test_password");
        }
    }

    @BeforeAll
    static void checkDocker() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker is not available; skipping PostgreSQL Testcontainers integration test");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (DockerClientFactory.instance().isDockerAvailable() && postgres != null && postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        } else {
            // Fallback mock/dummy properties if Docker is absent to prevent context failure before skip
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:postgres_stub;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
        }
    }

    @Test
    void postgreSqlContextLoads() {
        // Verifies context initialization against PostgreSQL 17 when Docker is available
    }
}

