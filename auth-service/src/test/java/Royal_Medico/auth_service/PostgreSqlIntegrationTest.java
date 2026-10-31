package Royal_Medico.auth_service;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.exception.BusinessIdentityCollisionException;
import Royal_Medico.auth_service.repository.*;
import Royal_Medico.auth_service.service.PharmacyRegistrationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("postgres-it")
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgreSqlIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres;

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            postgres = new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("auth_user_test")
                    .withUsername("test_user")
                    .withPassword("test_password");
            postgres.start();
        }
    }

    @BeforeAll
    static void checkDockerAndSafetyGuard() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable() && postgres != null && postgres.isRunning(),
                "Docker is not available or PostgreSQL container failed to start; skipping PostgreSQL integration test");

        // CRITICAL SAFETY GUARD: Must refuse to execute against auth_user or any unapproved database
        String dbName = postgres.getDatabaseName();
        if ("auth_user".equalsIgnoreCase(dbName) || !dbName.endsWith("_test")) {
            throw new IllegalStateException("CRITICAL SAFETY GUARD VIOLATION: Refusing to run tests against live or unapproved database: " + dbName);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres != null && postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:postgres_stub;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PharmacyRegistrationService registrationService;

    @Autowired
    private PharmacyRegistrationRepository registrationRepository;

    @Autowired
    private BusinessIdentityRepository identityRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TenantMembershipRepository tenantMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User superAdminUser;

    @BeforeEach
    void setUpSuperAdmin() {
        // Enforce safety guard check dynamically on JDBC connection
        String currentDb = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
        if ("auth_user".equalsIgnoreCase(currentDb) || currentDb == null || !currentDb.endsWith("_test")) {
            throw new IllegalStateException("CRITICAL SAFETY GUARD VIOLATION: Current database is " + currentDb + ". Refusing execution!");
        }

        superAdminUser = userRepository.findByEmail("pg.superadmin@royalmedico.com")
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setEmail("pg.superadmin@royalmedico.com");
                    admin.setPassword("HashedPass123!");
                    admin.setName("Postgres Super Admin");
                    admin.setRole(Role.SUPER_ADMIN);
                    admin.setActive(true);
                    return userRepository.saveAndFlush(admin);
                });
    }

    @Test
    @Order(1)
    @DisplayName("Safety Guard: Verifies isolated PostgreSQL 17 test database and refuses live auth_user")
    void test01_safetyGuardVerification() {
        String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
        String currentDb = jdbcTemplate.queryForObject("SELECT current_database()", String.class);

        assertNotNull(dbVersion);
        assertTrue(dbVersion.contains("PostgreSQL 17"), "Database must be PostgreSQL 17; actual: " + dbVersion);
        assertEquals("auth_user_test", currentDb, "Database name must strictly be 'auth_user_test'");
        assertNotEquals("auth_user", currentDb, "Database MUST NOT be the live 'auth_user' database");
    }

    @Test
    @Order(2)
    @DisplayName("Flyway Migration: Verifies complete execution of V1, V2, V3, and V4 migrations")
    void test02_flywayMigrationSequenceVerification() {
        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
                "SELECT version, description, type, script, success FROM flyway_schema_history ORDER BY installed_rank");

        assertEquals(4, migrations.size(), "All 4 Flyway migrations must be installed");

        assertEquals("1", migrations.get(0).get("version"));
        assertEquals("init", migrations.get(0).get("description"));
        assertTrue((Boolean) migrations.get(0).get("success"));

        assertEquals("2", migrations.get(1).get("version"));
        assertEquals("tenants and memberships", migrations.get(1).get("description"));
        assertTrue((Boolean) migrations.get(1).get("success"));

        assertEquals("3", migrations.get(2).get("version"));
        assertEquals("staff single tenant constraint", migrations.get(2).get("description"));
        assertTrue((Boolean) migrations.get(2).get("success"));

        assertEquals("4", migrations.get(3).get("version"));
        assertEquals("pharmacy onboarding and governance", migrations.get(3).get("description"));
        assertTrue((Boolean) migrations.get(3).get("success"));
    }

    @Test
    @Order(3)
    @DisplayName("Schema Structure: Verifies V4 tables, columns, indexes, constraints, and PL/pgSQL triggers")
    void test03_schemaStructureAndObjectsVerification() {
        // Tables
        Integer regTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'pharmacy_registrations'", Integer.class);
        Integer idTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'business_identities'", Integer.class);
        assertEquals(1, regTableCount);
        assertEquals(1, idTableCount);

        // Columns in tenants table
        List<String> tenantColumns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'tenants'", String.class);
        assertTrue(tenantColumns.contains("registration_id"));
        assertTrue(tenantColumns.contains("primary_owner_id"));
        assertTrue(tenantColumns.contains("primary_owner_membership_id"));
        assertTrue(tenantColumns.contains("gstin"));
        assertTrue(tenantColumns.contains("pan"));
        assertTrue(tenantColumns.contains("drug_license_number"));

        // Columns in tenant_memberships
        List<String> membershipColumns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'tenant_memberships'", String.class);
        assertTrue(membershipColumns.contains("is_primary_owner"));
        assertTrue(membershipColumns.contains("security_version"));

        // Partial unique indexes
        List<String> indexes = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE schemaname = 'public'", String.class);
        assertTrue(indexes.contains("uk_active_business_identity"));
        assertTrue(indexes.contains("uk_tenant_single_primary_owner"));
        assertTrue(indexes.contains("uk_user_active_staff_membership"));

        // PL/pgSQL triggers
        List<String> triggers = jdbcTemplate.queryForList(
                "SELECT trigger_name FROM information_schema.triggers WHERE trigger_schema = 'public'", String.class);
        assertTrue(triggers.contains("trg_membership_lock_tenants_before"));
        assertTrue(triggers.contains("trg_enforce_tenant_primary_owner_deferred"));
        assertTrue(triggers.contains("trg_enforce_membership_primary_owner_deferred"));
    }

    @Test
    @Order(4)
    @DisplayName("Primary Owner Invariants: Verifies trigger enforcement (single primary owner, role OWNER, status ACTIVE)")
    void test04_primaryOwnerTriggerGovernance() {
        // 1. Create User and Active Tenant
        Long testUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, name, role, is_active, created_at, updated_at) " +
                "VALUES ('owner1@pgtest.com', 'Pass123!', 'Owner One', 'CUSTOMER', true, NOW(), NOW()) RETURNING id", Long.class);

        Long tenantId = jdbcTemplate.queryForObject(
                "INSERT INTO tenants (name, slug, status, created_at, updated_at) " +
                "VALUES ('Test Pharmacy PG', 'test-pharmacy-pg', 'ACTIVE', NOW(), NOW()) RETURNING id", Long.class);

        // 2 & 3. Atomically create primary owner membership and link tenant pointers in one transaction
        Long membershipId = jdbcTemplate.execute((Connection conn) -> {
            conn.setAutoCommit(false);
            Long mId;
            try (var ps1 = conn.prepareStatement(
                    "INSERT INTO tenant_memberships (user_id, tenant_id, role, status, is_primary_owner, created_at, updated_at) " +
                    "VALUES (?, ?, 'OWNER', 'ACTIVE', true, NOW(), NOW()) RETURNING id")) {
                ps1.setLong(1, testUserId);
                ps1.setLong(2, tenantId);
                try (var rs = ps1.executeQuery()) {
                    rs.next();
                    mId = rs.getLong(1);
                }
            }

            try (var ps2 = conn.prepareStatement(
                    "UPDATE tenants SET primary_owner_id = ?, primary_owner_membership_id = ? WHERE id = ?")) {
                ps2.setLong(1, testUserId);
                ps2.setLong(2, mId);
                ps2.setLong(3, tenantId);
                ps2.executeUpdate();
            }

            conn.commit();
            return mId;
        });

        // 4. Attempt to insert a SECOND primary owner on the same tenant -> rejected by partial unique index
        Long secondUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, name, role, is_active, created_at, updated_at) " +
                "VALUES ('owner2@pgtest.com', 'Pass123!', 'Owner Two', 'CUSTOMER', true, NOW(), NOW()) RETURNING id", Long.class);

        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update(
                    "INSERT INTO tenant_memberships (user_id, tenant_id, role, status, is_primary_owner, created_at, updated_at) " +
                    "VALUES (?, ?, 'OWNER', 'ACTIVE', true, NOW(), NOW())", secondUserId, tenantId);
        }, "Inserting a second primary owner must violate uk_tenant_single_primary_owner");

        // 5. Attempt to demote the primary owner to PHARMACY_STAFF -> rejected by deferred constraint trigger
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute((Connection conn) -> {
                conn.setAutoCommit(false);
                try (var stmt = conn.prepareStatement("UPDATE tenant_memberships SET role = 'PHARMACY_STAFF' WHERE id = ?")) {
                    stmt.setLong(1, membershipId);
                    stmt.executeUpdate();
                }
                conn.commit(); // Deferred trigger evaluates at commit time
                return null;
            });
        }, "Demoting primary owner to non-OWNER role must violate deferred invariant trigger");
    }

    @Test
    @Order(5)
    @DisplayName("Statutory Identity Uniqueness: Verifies partial unique index on (RESERVED, ACTIVE) and RELEASED re-use")
    void test05_statutoryIdentityUniquenessAndLifecycle() {
        String testGstin = "27AABCA3333A1Z8";

        // Create a dummy registration
        Long regId = jdbcTemplate.queryForObject(
                "INSERT INTO pharmacy_registrations (legal_business_name, trade_name, proposed_slug, proposed_owner_email, " +
                "proposed_owner_name, address_line_1, city, state, postal_code, status, created_at, updated_at) " +
                "VALUES ('Med Ltd', 'Med Store', 'med-store-pg', 'owner@med.com', 'Owner Med', 'Addr 1', 'Pune', 'Maharashtra', '411001', " +
                "'PENDING_VERIFICATION', NOW(), NOW()) RETURNING id", Long.class);

        // 1. Insert RESERVED identity
        Long id1 = jdbcTemplate.queryForObject(
                "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                "VALUES ('GSTIN', ?, 'REGISTRATION', ?, 'RESERVED', NOW(), NOW()) RETURNING id", Long.class, testGstin, regId);
        assertNotNull(id1);

        // 2. Duplicate insert with RESERVED status -> must violate uk_active_business_identity
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update(
                    "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                    "VALUES ('GSTIN', ?, 'REGISTRATION', ?, 'RESERVED', NOW(), NOW())", testGstin, regId);
        });

        // 3. Duplicate insert with ACTIVE status -> must also violate uk_active_business_identity
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update(
                    "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                    "VALUES ('GSTIN', ?, 'REGISTRATION', ?, 'ACTIVE', NOW(), NOW())", testGstin, regId);
        });

        // 4. Update status to RELEASED
        jdbcTemplate.update("UPDATE business_identities SET status = 'RELEASED' WHERE id = ?", id1);

        // 5. Subsequent insert with same GSTIN and RESERVED status must now SUCCEED
        Long id2 = jdbcTemplate.queryForObject(
                "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                "VALUES ('GSTIN', ?, 'REGISTRATION', ?, 'RESERVED', NOW(), NOW()) RETURNING id", Long.class, testGstin, regId);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    @Order(6)
    @DisplayName("Statutory Check Constraints: Verifies PostgreSQL format and normalization checks")
    void test06_statutoryCheckConstraints() {
        Long regId = jdbcTemplate.queryForObject(
                "SELECT id FROM pharmacy_registrations WHERE proposed_slug = 'med-store-pg'", Long.class);

        // 1. Invalid GSTIN format
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update(
                    "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                    "VALUES ('GSTIN', 'INVALID_GSTIN_123', 'REGISTRATION', ?, 'RESERVED', NOW(), NOW())", regId);
        }, "Must violate chk_business_identity_format_gstin");

        // 2. Invalid PAN format
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update(
                    "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                    "VALUES ('BUSINESS_PAN', 'INVALID_PAN', 'REGISTRATION', ?, 'RESERVED', NOW(), NOW())", regId);
        }, "Must violate chk_business_identity_format_pan");

        // 3. Lowercase / non-normalized identifier
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update(
                    "INSERT INTO business_identities (identifier_type, identifier_value, entity_type, registration_id, status, created_at, updated_at) " +
                    "VALUES ('BUSINESS_PAN', 'aabca1234a', 'REGISTRATION', ?, 'RESERVED', NOW(), NOW())", regId);
        }, "Must violate chk_business_identity_normalized");
    }

    @Test
    @Order(7)
    @DisplayName("Transaction Rollback: Verifies atomic rollback on registration failure (leaves 0 orphan identities)")
    void test07_transactionRollbackOnFailure() {
        long initialRegCount = registrationRepository.count();
        long initialIdCount = identityRepository.count();

        // Prepare request with duplicate slug that already exists in tenants table
        PharmacyRegistrationRequest request = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Rollback Pharma Ltd")
                .tradeName("Rollback Pharma")
                .proposedSlug("test-pharmacy-pg") // Collides with existing tenant slug from test04
                .proposedOwnerName("Owner Rollback")
                .proposedOwnerEmail("rollback@pharma.com")
                .addressLine1("123 Street")
                .city("Mumbai")
                .state("Maharashtra")
                .postalCode("400001")
                .gstin("27AABCR9999R1Z1")
                .pan("AABCR9999R")
                .drugLicenseForm20("MH-BOM-999999")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                registrationService.initiateRegistration(request, superAdminUser));

        // Assert zero records created
        assertEquals(initialRegCount, registrationRepository.count(), "No registration record must be left on rollback");
        assertEquals(initialIdCount, identityRepository.count(), "No identity record must be left on rollback");
    }

    @Test
    @Order(8)
    @DisplayName("End-to-End & Concurrency on PostgreSQL 17: Verifies registration, 8 identities, and concurrent collision")
    void test08_endToEndPharmacyRegistrationAndConcurrencyOnPostgreSql17() throws InterruptedException {
        String sharedGstin = "27AABCE8888E1Z9";

        PharmacyRegistrationRequest req1 = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Apollo Thane Ltd")
                .tradeName("Apollo Pharmacy Thane")
                .proposedSlug("apollo-pharmacy-thane")
                .proposedOwnerName("Sanjay Patel")
                .proposedOwnerEmail("sanjay@apollothane.com")
                .addressLine1("Gokhale Road")
                .city("Thane")
                .state("Maharashtra")
                .postalCode("400601")
                .gstin(sharedGstin)
                .pan("AABCE8888E")
                .drugLicenseForm20("MH-TZ1-00001")
                .drugLicenseForm21("MH-TZ1-00002")
                .drugLicenseForm20B("MH-TZ1-00003")
                .drugLicenseForm21B("MH-TZ1-00004")
                .drugLicenseForm20C("MH-TZ1-00005")
                .fssai("11521012000999")
                .build();

        PharmacyRegistrationRequest req2 = PharmacyRegistrationRequest.builder()
                .legalBusinessName("MedPlus Thane Ltd")
                .tradeName("MedPlus Thane")
                .proposedSlug("medplus-thane")
                .proposedOwnerName("Kishore Kumar")
                .proposedOwnerEmail("kishore@medplus.com")
                .addressLine1("Station Road")
                .city("Thane")
                .state("Maharashtra")
                .postalCode("400602")
                .gstin(sharedGstin) // Duplicate GSTIN
                .pan("AABCM7777M")
                .drugLicenseForm20("MH-TZ1-00006")
                .build();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger collisionCount = new AtomicInteger(0);

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                registrationService.initiateRegistration(req1, superAdminUser);
                successCount.incrementAndGet();
            } catch (BusinessIdentityCollisionException e) {
                collisionCount.incrementAndGet();
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                registrationService.initiateRegistration(req2, superAdminUser);
                successCount.incrementAndGet();
            } catch (BusinessIdentityCollisionException e) {
                collisionCount.incrementAndGet();
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Both concurrent threads must finish within timeout");
        assertEquals(1, successCount.get(), "Exactly one registration must succeed on PostgreSQL 17");
        assertEquals(1, collisionCount.get(), "The concurrent registration must be rejected with BusinessIdentityCollisionException on PostgreSQL 17");

        // Verify that exactly 1 RESERVED record exists for shared GSTIN
        Integer reservedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM business_identities WHERE identifier_type = 'GSTIN' AND identifier_value = ? AND status = 'RESERVED'",
                Integer.class, sharedGstin);
        assertEquals(1, reservedCount, "Exactly one identity record must be reserved in PostgreSQL 17");
    }
}
