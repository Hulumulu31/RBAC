import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Планировщик периодических задач для RBAC-системы.
 * Использует ScheduledExecutorService для:
 * - Поиска и деактивации истёкших временных назначений
 * - Логирования статистики системы
 */
public class ScheduledTaskManager {
    private final ScheduledExecutorService scheduler;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private final RBACSystem rbacSystem;
    private final AtomicBoolean running;

    /**
     * Создаёт планировщик задач.
     *
     * @param assignmentManager менеджер назначений
     * @param auditLog журнал аудита
     * @param rbacSystem основная система
     */
    public ScheduledTaskManager(AssignmentManager assignmentManager, AuditLog auditLog, RBACSystem rbacSystem) {
        this.assignmentManager = assignmentManager;
        this.auditLog = auditLog;
        this.rbacSystem = rbacSystem;
        this.running = new AtomicBoolean(false);
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("rbac-scheduler-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Запускает периодическую задачу поиска и деактивации истёкших назначений.
     *
     * @param intervalSeconds интервал выполнения в секундах
     */
    public void startExpiredAssignmentChecker(long intervalSeconds) {
        if (!running.compareAndSet(false, true)) {
            auditLog.log("SCHEDULER_WARN", "system", "ExpiredAssignmentChecker",
                "Checker already running");
            return;
        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Короткая критическая секция — получаем список истёкших назначений
                List<RoleAssignment> expiredAssignments;
                synchronized (assignmentManager) {
                    expiredAssignments = assignmentManager.getExpiredAssignments();
                }

                int deactivatedCount = 0;
                for (RoleAssignment assignment : expiredAssignments) {
                    if (assignment instanceof TemporaryAssignment tempAssignment) {
                        if (tempAssignment.isExpired()) {
                            // Временное назначение уже неактивно по логике isActive()
                            // Логируем это
                            auditLog.log("EXPIRED_DETECTED", "scheduler",
                                assignment.user().username(),
                                "Temporary assignment expired for role: " + assignment.role().getName());
                            deactivatedCount++;
                        }
                    }
                }

                if (deactivatedCount > 0) {
                    auditLog.log("EXPIRED_SUMMARY", "scheduler", "system",
                        "Deactivated " + deactivatedCount + " expired assignments");
                }
            } catch (Exception e) {
                auditLog.log("SCHEDULER_ERROR", "system", "ExpiredAssignmentChecker",
                    "Error: " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);

        auditLog.log("SCHEDULER_START", "system", "ExpiredAssignmentChecker",
            "Started checker with interval: " + intervalSeconds + "s");
    }

    /**
     * Запускает периодическую задачу логирования статистики.
     *
     * @param intervalSeconds интервал выполнения в секундах
     */
    public void startStatisticsLogger(long intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Минимальные блокировки — собираем статистику короткими секциями
                int userCount;
                int roleCount;
                int assignmentCount;
                int activeCount;

                synchronized (rbacSystem.getUserManager()) {
                    userCount = rbacSystem.getUserManager().count();
                }
                synchronized (rbacSystem.getRoleManager()) {
                    roleCount = rbacSystem.getRoleManager().count();
                }
                synchronized (assignmentManager) {
                    assignmentCount = assignmentManager.count();
                    activeCount = (int) assignmentManager.getActiveAssignments().size();
                }

                String stats = String.format("Users: %d, Roles: %d, Assignments: %d (Active: %d)",
                    userCount, roleCount, assignmentCount, activeCount);

                auditLog.log("STATS_PERIODIC", "scheduler", "system", stats);
            } catch (Exception e) {
                auditLog.log("SCHEDULER_ERROR", "system", "StatisticsLogger",
                    "Error: " + e.getMessage());
            }
        }, 5, intervalSeconds, TimeUnit.SECONDS);

        auditLog.log("SCHEDULER_START", "system", "StatisticsLogger",
            "Started stats logger with interval: " + intervalSeconds + "s");
    }

    /**
     * Останавливает все запланированные задачи.
     */
    public void shutdown() {
        running.set(false);
        auditLog.log("SCHEDULER_STOP", "system", "ScheduledTaskManager",
            "Shutting down scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Проверяет, запущен ли планировщик.
     */
    public boolean isRunning() {
        return running.get();
    }
}
