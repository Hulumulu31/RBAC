import java.util.concurrent.*;

/**
 * Фоновый исполнитель задач для RBAC-системы.
 * Использует ExecutorService для выполнения асинхронных операций.
 */
public class BackgroundExecutor {
    private final ExecutorService executorService;
    private final AuditLog auditLog;

    /**
     * Создаёт новый BackgroundExecutor с фиксированным пулом потоков.
     *
     * @param poolSize    размер пула потоков
     * @param auditLog журнал аудита для логирования операций
     */
    public BackgroundExecutor(int poolSize, AuditLog auditLog) {
        this.auditLog = auditLog;
        this.executorService = Executors.newFixedThreadPool(poolSize, r -> {
            Thread thread = new Thread(r);
            thread.setName("rbac-worker-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Запускает задачу в фоновом потоке.
     *
     * @param task задача для выполнения
     * @param taskName имя задачи (для логирования)
     */
    public void submitTask(Runnable task, String taskName) {
        executorService.submit(() -> {
            try {
                auditLog.log("TASK_START", "system", taskName, "Background task started");
                task.run();
                auditLog.log("TASK_COMPLETE", "system", taskName, "Background task completed successfully");
            } catch (Exception e) {
                auditLog.log("TASK_ERROR", "system", taskName, "Error: " + e.getMessage());
            }
        });
    }

    /**
     * Запускает задачу с возвращаемым результатом.
     *
     * @param task    задача для выполнения
     * @param taskName имя задачи (для логирования)
     * @param <T>      тип результата
     * @return Future для получения результата
     */
    public <T> Future<T> submitTaskWithResult(Callable<T> task, String taskName) {
        return executorService.submit(() -> {
            auditLog.log("TASK_START", "system", taskName, "Background task with result started");
            try {
                T result = task.call();
                auditLog.log("TASK_COMPLETE", "system", taskName, "Background task with result completed");
                return result;
            } catch (Exception e) {
                auditLog.log("TASK_ERROR", "system", taskName, "Error: " + e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Проверяет, был ли исполнитель завершён.
     *
     * @return true, если исполнитель завершён
     */
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    /**
     * Плавно завершает все задачи.
     */
    public void shutdown() {
        auditLog.log("SHUTDOWN", "system", "BackgroundExecutor", "Initiating graceful shutdown");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                auditLog.log("SHUTDOWN", "system", "BackgroundExecutor", "Forced shutdown after timeout");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Возвращает количество активных задач.
     *
     * @return approximate number of tasks
     */
    public int getActiveTaskCount() {
        if (executorService instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executorService).getActiveCount();
        }
        return 0;
    }
}
