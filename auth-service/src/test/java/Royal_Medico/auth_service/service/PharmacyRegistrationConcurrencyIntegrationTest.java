package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.exception.BusinessIdentityCollisionException;
import Royal_Medico.auth_service.repository.BusinessIdentityRepository;
import Royal_Medico.auth_service.repository.PharmacyRegistrationRepository;
import Royal_Medico.auth_service.repository.TenantRepository;
import Royal_Medico.auth_service.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PharmacyRegistrationConcurrencyIntegrationTest {

    @Autowired
    private PharmacyRegistrationService registrationService;

    @Autowired
    private PharmacyRegistrationRepository registrationRepository;

    @Autowired
    private BusinessIdentityRepository identityRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private User superAdminUser;

    @BeforeEach
    void setUp() {
        identityRepository.deleteAllInBatch();
        registrationRepository.deleteAllInBatch();
        tenantRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User admin = new User();
        admin.setEmail("admin." + java.util.UUID.randomUUID() + "@royalmedico.com");
        admin.setPassword("HashedPass123!");
        admin.setName("Super Admin Test");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setActive(true);
        superAdminUser = userRepository.saveAndFlush(admin);
    }

    @AfterEach
    void tearDown() {
        identityRepository.deleteAllInBatch();
        registrationRepository.deleteAllInBatch();
        tenantRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Simultaneous registration attempts for the same statutory GSTIN serialize and reject duplicate with collision")
    void concurrentRegistrationsWithSameGstin_serializeAndDetectCollision() throws InterruptedException {
        String sharedGstin = "27AABCA9999A1Z1";
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger collisionCount = new AtomicInteger(0);

        // Request 1
        PharmacyRegistrationRequest req1 = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Chemist One Ltd")
                .tradeName("Chemist One")
                .proposedSlug("chemist-one")
                .proposedOwnerName("Owner One")
                .proposedOwnerEmail("owner1@chemistone.com")
                .addressLine1("1st Street")
                .city("Pune")
                .state("Maharashtra")
                .postalCode("411001")
                .gstin(sharedGstin)
                .pan("AABCA9999A")
                .drugLicenseForm20("MH-PUN-00001")
                .build();

        // Request 2 (different slug and details, but identical GSTIN)
        PharmacyRegistrationRequest req2 = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Chemist Two Ltd")
                .tradeName("Chemist Two")
                .proposedSlug("chemist-two")
                .proposedOwnerName("Owner Two")
                .proposedOwnerEmail("owner2@chemisttwo.com")
                .addressLine1("2nd Street")
                .city("Pune")
                .state("Maharashtra")
                .postalCode("411002")
                .gstin(sharedGstin)
                .pan("BBBCB8888B")
                .drugLicenseForm20("MH-PUN-00002")
                .build();

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                registrationService.initiateRegistration(req1, superAdminUser);
                successCount.incrementAndGet();
            } catch (BusinessIdentityCollisionException e) {
                collisionCount.incrementAndGet();
            } catch (Exception e) {
                // other exception
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
            } catch (Exception e) {
                // other exception
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Both concurrent registration threads should complete within timeout");
        assertEquals(1, successCount.get(), "Exactly one registration must succeed in reserving the GSTIN");
        assertEquals(1, collisionCount.get(), "The concurrent registration must be rejected with BusinessIdentityCollisionException");

        // Verify that exactly 1 RESERVED record exists for shared GSTIN in business_identities
        List<BusinessIdentity> identities = identityRepository.findAll();
        long gstinCount = identities.stream()
                .filter(bi -> bi.getIdentifierType() == BusinessIdentifierType.GSTIN && bi.getIdentifierValue().equals(sharedGstin))
                .count();
        assertEquals(1, gstinCount, "Exactly one identity record should exist in the registry for the GSTIN");
    }

    @Test
    @DisplayName("Releasing identities upon rejection allows subsequent registration with the released GSTIN")
    void rejectedRegistration_releasesGstinForSubsequentRegistration() {
        String testGstin = "27AABCA7777A1Z3";

        PharmacyRegistrationRequest initialReq = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Failed Chemist")
                .tradeName("Failed Store")
                .proposedSlug("failed-store")
                .proposedOwnerName("Owner Initial")
                .proposedOwnerEmail("initial@failedstore.com")
                .addressLine1("Street 9")
                .city("Nagpur")
                .state("Maharashtra")
                .postalCode("440001")
                .gstin(testGstin)
                .pan("AABCA7777A")
                .drugLicenseForm20("MH-NAG-12345")
                .build();

        // 1. Initial registration reserves GSTIN
        PharmacyRegistrationResponse initialRes = registrationService.initiateRegistration(initialReq, superAdminUser);
        assertEquals("PENDING_VERIFICATION", initialRes.getStatus());

        // 2. Reject registration -> status becomes REJECTED and identities become RELEASED
        registrationService.rejectRegistration(initialRes.getId(), 
                new RejectRegistrationRequest("Application rejected by FDA inspector"), superAdminUser);

        // 3. Subsequent application with the SAME GSTIN must now succeed
        PharmacyRegistrationRequest subsequentReq = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Success Chemist")
                .tradeName("Success Store")
                .proposedSlug("success-store")
                .proposedOwnerName("Owner Success")
                .proposedOwnerEmail("success@store.com")
                .addressLine1("Street 10")
                .city("Nagpur")
                .state("Maharashtra")
                .postalCode("440001")
                .gstin(testGstin)
                .pan("AABCA7777A")
                .drugLicenseForm20("MH-NAG-12345")
                .build();

        PharmacyRegistrationResponse subsequentRes = registrationService.initiateRegistration(subsequentReq, superAdminUser);
        assertNotNull(subsequentRes);
        assertEquals("PENDING_VERIFICATION", subsequentRes.getStatus());
        assertEquals("success-store", subsequentRes.getProposedSlug());
    }

    @Test
    @DisplayName("Simultaneous registration attempts for the same statutory PAN serialize and reject duplicate with collision")
    void concurrentRegistrationsWithSamePan_serializeAndDetectCollision() throws InterruptedException {
        String sharedPan = "AABCA8888A";
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger collisionCount = new AtomicInteger(0);

        PharmacyRegistrationRequest req1 = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Pharma Alpha Ltd")
                .tradeName("Pharma Alpha")
                .proposedSlug("pharma-alpha")
                .proposedOwnerName("Owner Alpha")
                .proposedOwnerEmail("owner@alpha.com")
                .addressLine1("Alpha Street")
                .city("Nashik")
                .state("Maharashtra")
                .postalCode("422001")
                .gstin("27AABCA1111A1Z1")
                .pan(sharedPan)
                .drugLicenseForm20("MH-NSK-00001")
                .build();

        PharmacyRegistrationRequest req2 = PharmacyRegistrationRequest.builder()
                .legalBusinessName("Pharma Beta Ltd")
                .tradeName("Pharma Beta")
                .proposedSlug("pharma-beta")
                .proposedOwnerName("Owner Beta")
                .proposedOwnerEmail("owner@beta.com")
                .addressLine1("Beta Street")
                .city("Nashik")
                .state("Maharashtra")
                .postalCode("422002")
                .gstin("27AABCA2222A1Z2")
                .pan(sharedPan) // Identical PAN
                .drugLicenseForm20("MH-NSK-00002")
                .build();

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
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Both concurrent registration threads should complete within timeout");
        assertEquals(1, successCount.get(), "Exactly one registration must succeed in reserving the PAN");
        assertEquals(1, collisionCount.get(), "The concurrent registration must be rejected with BusinessIdentityCollisionException");

        List<BusinessIdentity> identities = identityRepository.findAll();
        long panCount = identities.stream()
                .filter(bi -> bi.getIdentifierType() == BusinessIdentifierType.BUSINESS_PAN && bi.getIdentifierValue().equals(sharedPan))
                .count();
        assertEquals(1, panCount, "Exactly one identity record should exist in the registry for the PAN");
    }
}
