package Royal_Medico.auth_service.service;

import Royal_Medico.auth_service.entity.*;
import Royal_Medico.auth_service.repository.TenantMembershipRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class TenantMembershipConcurrencyIntegrationTest {

    @Autowired
    private TenantMembershipService tenantMembershipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TenantMembershipRepository tenantMembershipRepository;

    private User testUser;
    private Tenant tenant1;
    private Tenant tenant2;

    @BeforeEach
    void setUp() {
        tenantMembershipRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("staff.concurrent@royalmedico.com");
        user.setPassword("EncodedPassword123!");
        user.setName("Concurrent Staff");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        testUser = userRepository.save(user);

        Tenant t1 = new Tenant("Alpha Pharmacy", "alpha-pharmacy");
        t1.setStatus(TenantStatus.ACTIVE);
        tenant1 = tenantRepository.save(t1);

        Tenant t2 = new Tenant("Beta Pharmacy", "beta-pharmacy");
        t2.setStatus(TenantStatus.ACTIVE);
        tenant2 = tenantRepository.save(t2);
    }

    @AfterEach
    void tearDown() {
        tenantMembershipRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrent membership creation for staff across different tenants cannot violate single-tenant rule")
    void concurrentStaffCreationCannotViolateRule() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Task 1: Attempt to create Staff membership in Tenant 1
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                tenantMembershipService.createMembership(testUser.getId(), tenant1.getId(), TenantRole.PHARMACY_STAFF);
                successCount.incrementAndGet();
            } catch (IllegalStateException e) {
                failureCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        // Task 2: Attempt to create Staff membership in Tenant 2 simultaneously
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                tenantMembershipService.createMembership(testUser.getId(), tenant2.getId(), TenantRole.PHARMACY_STAFF);
                successCount.incrementAndGet();
            } catch (IllegalStateException e) {
                failureCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        // Ensure both threads are ready, then fire simultaneously
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        boolean completedInTime = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly one must succeed and one must fail
        assertEquals(true, completedInTime, "Concurrent tasks should complete within timeout");
        assertEquals(1, successCount.get(), "Expected exactly 1 successful staff membership creation");
        assertEquals(1, failureCount.get(), "Expected exactly 1 rejected staff membership creation");

        // Verify database contains only 1 membership
        List<TenantMembership> memberships = tenantMembershipRepository.findByUserId(testUser.getId());
        assertEquals(1, memberships.size(), "User must have exactly 1 active membership in database");
        assertEquals(TenantRole.PHARMACY_STAFF, memberships.get(0).getRole());
        assertEquals(MembershipStatus.ACTIVE, memberships.get(0).getStatus());
    }
}

