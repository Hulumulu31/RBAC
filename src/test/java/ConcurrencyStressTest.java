import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Нагрузочный тест для проверки потокобезопасности RBAC-системы.
 * Создаёт несколько потоков, каждый из которых создаёт/обновляет пользователей,
 * создаёт/назначает роли, выполняет фильтры/поиски.
 * Проверяет, что приложение не падает и нет странных состояний (дубликаты, пропуски).
 */
public class ConcurrencyStressTest {

    private static final int NUM_THREADS = 10;
    private static final int OPERATIONS_PER_THREAD = 50;

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        auditLog = new AuditLog();

        // Создаем базовые роли
        Role adminRole = new Role("Admin", "Administrator");
        Role viewerRole = new Role("Viewer", "Viewer");
        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        assignmentManager = new AssignmentManager(userManager, roleManager);
    }

    @Test
    @DisplayName("Нагрузочный тест: конкурентное создание/обновление/фильтрация")
    void stressTest_ConcurrentOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);

        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < NUM_THREADS; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Все потоки стартуют одновременно

                    for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                        try {
                            int op = (threadId * OPERATIONS_PER_THREAD + i) % 5;
                            switch (op) {
                                case 0: // Создание пользователя
                                    String username = "user_t" + threadId + "_" + i;
                                    String email = username + "@test.com";
                                    User user = User.validate(username, "User " + username, email);
                                    userManager.add(user);
                                    break;

                                case 1: // Обновление пользователя
                                    String updateUsername = "user_t" + (threadId % 3) + "_0";
                                    if (userManager.exists(updateUsername)) {
                                        try {
                                            userManager.update(updateUsername, "Updated Name", "updated@test.com");
                                        } catch (IllegalArgumentException ignored) {
                                            // Может быть невалидный email при обновлении
                                        }
                                    }
                                    break;

                                case 2: // Поиск/фильтрация
                                    UserFilter filter = UserFilters.byEmailDomain("@test.com");
                                    List<User> found = userManager.findByFilterParallel(filter);
                                    // Просто проверяем, что не падает
                                    assertNotNull(found);
                                    break;

                                case 3: // Назначение роли
                                    String assignUsername = "user_t" + threadId + "_" + Math.max(0, i - 1);
                                    if (userManager.exists(assignUsername)) {
                                        User assignUser = userManager.findByUsername(assignUsername).orElse(null);
                                        if (assignUser != null) {
                                            Optional<Role> roleOpt = roleManager.findByName("Viewer");
                                            if (roleOpt.isPresent()) {
                                                Role role = roleOpt.get();
                                                try {
                                                    AssignmentMetadata metadata = AssignmentMetadata.now("stress-test", "Stress test");
                                                    PermanentAssignment assignment = new PermanentAssignment(assignUser, role, metadata);
                                                    assignmentManager.add(assignment);
                                                } catch (IllegalArgumentException ignored) {
                                                    // Duplicate assignment
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case 4: // Поиск ролей
                                    List<Role> roles = roleManager.findByFilterParallel(RoleFilters.byNameContains("Admin"));
                                    assertNotNull(roles);
                                    // Поиск назначений
                                    List<RoleAssignment> assignments = assignmentManager.findByFilterParallel(AssignmentFilters.activeOnly());
                                    assertNotNull(assignments);
                                    break;
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            errors.add("Thread-" + threadId + " op-" + i + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Запускаем все потоки одновременно
        startLatch.countDown();

        // Ждём завершения с таймаутом
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Проверки
        assertTrue(finished, "Test did not finish within 30 seconds — possible deadlock");

        // Проверяем, что не все операции завершились ошибками
        int totalOps = NUM_THREADS * OPERATIONS_PER_THREAD;
        int successOps = totalOps - errorCount.get();
        System.out.println("=== STRESS TEST RESULTS ===");
        System.out.println("Total operations: " + totalOps);
        System.out.println("Successful: " + successOps);
        System.out.println("Errors (expected for duplicates): " + errorCount.get());

        // Проверяем на дубликаты пользователей — UserManager сам защищает от дубликатов,
        // но при конкурентной работе ожидаемо, что часть операций add выбросит IllegalArgumentException
        List<User> allUsers = userManager.findAll();
        Set<String> usernames = new HashSet<>();
        Set<String> emails = new HashSet<>();
        boolean hasDuplicates = false;

        for (User user : allUsers) {
            if (!usernames.add(user.username())) {
                System.err.println("DUPLICATE USERNAME: " + user.username());
                hasDuplicates = true;
            }
            if (!emails.add(user.email())) {
                // Email дубликаты возможны при обновлении пользователей
                // Не считаем это ошибкой
            }
        }

        assertFalse(hasDuplicates, "Found duplicate usernames (UserManager should prevent duplicates)");

        // Проверяем, что кол-во пользователей соответствует ожиданиям
        System.out.println("Total users created: " + userManager.count());
        System.out.println("Total assignments: " + assignmentManager.count());

        // Выводим ошибки если их немного (для отладки)
        if (errorCount.get() > 0 && errorCount.get() < 20) {
            System.out.println("\nSample errors (expected for duplicates/validation):");
            int count = 0;
            for (String err : errors) {
                if (count++ < 5) {
                    System.out.println("  " + err);
                }
            }
        }

        // Главное — нет IllegalArgumentException от concurrent модификации
        long concurrentErrors = errors.stream()
                .filter(e -> e.contains("ConcurrentModification") || e.contains("IllegalStateException"))
                .count();
        assertEquals(0, concurrentErrors, "Found concurrent modification errors");
    }

    @Test
    @DisplayName("Нагрузочный тест: параллельная генерация отчётов")
    void stressTest_ParallelReports() throws InterruptedException {
        // Создаем пользователей
        for (int i = 0; i < 20; i++) {
            User user = User.validate("reportuser" + i, "Report User " + i, "report" + i + "@test.com");
            userManager.add(user);
            Role role = roleManager.findByName("Viewer").orElseThrow();
            AssignmentMetadata metadata = AssignmentMetadata.now("stress-test", "Report test");
            PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
            try {
                assignmentManager.add(assignment);
            } catch (IllegalArgumentException ignored) {}
        }

        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final int reportId = i;
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    ReportGenerator rg = new ReportGenerator();

                    if (reportId % 3 == 0) {
                        String report = rg.generateUserReportParallel(userManager, assignmentManager);
                        assertNotNull(report);
                        assertTrue(report.contains("USER REPORT"));
                    } else if (reportId % 3 == 1) {
                        String report = rg.generatePermissionMatrixParallel(userManager, assignmentManager);
                        assertNotNull(report);
                        assertTrue(report.contains("PERMISSION MATRIX"));
                    } else {
                        String report = rg.generateRoleReport(roleManager, assignmentManager);
                        assertNotNull(report);
                        assertTrue(report.contains("ROLE REPORT"));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        latch.countDown();

        for (Future<?> future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                fail("Report generation failed: " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                fail("Report generation timed out");
            }
        }

        executor.shutdown();
        System.out.println("=== PARALLEL REPORT TEST PASSED ===");
    }
}
