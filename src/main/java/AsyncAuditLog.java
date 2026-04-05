import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Потокобезопасная система логирования с асинхронной обработкой через очередь.
 * Записи добавляются в очередь и обрабатываются отдельным потоком.
 */
public class AsyncAuditLog {
    private final BlockingQueue<AuditEntry> queue;
    private final List<AuditEntry> entries;
    private final Thread workerThread;
    private final AtomicBoolean running;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Запись аудита.
     */
    public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
    ) {}

    /**
     * Создаёт новый асинхронный журнал аудита.
     *
     * @param queueCapacity максимальный размер очереди
     */
    public AsyncAuditLog(int queueCapacity) {
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.entries = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(true);

        this.workerThread = new Thread(() -> {
            while (running.get() || !queue.isEmpty()) {
                try {
                    AuditEntry entry = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "async-audit-log-worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    /**
     * Создаёт журнал с ёмкостью очереди по умолчанию (10000).
     */
    public AsyncAuditLog() {
        this(10000);
    }

    /**
     * Асинхронно добавляет запись в журнал (неблокирующий).
     *
     * @param action действие
     * @param performer исполнитель
     * @param target цель
     * @param details детали
     * @return true, если запись успешно добавлена в очередь
     */
    public boolean logAsync(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        return queue.offer(entry);
    }

    /**
     * Синхронно добавляет запись (ждёт размещения в очереди).
     */
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        try {
            queue.put(entry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Возвращает все записи журнала.
     */
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    /**
     * Возвращает записи по исполнителю.
     */
    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null || performer.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return entries.stream()
                .filter(entry -> entry.performer().equalsIgnoreCase(performer.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает записи по действию.
     */
    public List<AuditEntry> getByAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return entries.stream()
                .filter(entry -> entry.action().equalsIgnoreCase(action.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Выводит все записи в консоль.
     */
    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Audit log is empty.");
            return;
        }

        System.out.println("\n=== ASYNC AUDIT LOG ===");
        System.out.println("Total entries: " + entries.size());
        System.out.println("Queue size: " + queue.size());
        System.out.println("+---------------------+------------------+------------------+------------------+");
        System.out.println("| Timestamp           | Action           | Performer        | Target           |");
        System.out.println("+---------------------+------------------+------------------+------------------+");

        for (AuditEntry entry : entries) {
            String timestamp = FormatUtils.truncate(entry.timestamp(), 19);
            String action = FormatUtils.truncate(entry.action(), 16);
            String performer = FormatUtils.truncate(entry.performer(), 16);
            String target = FormatUtils.truncate(entry.target(), 16);

            System.out.printf("| %-19s | %-16s | %-16s | %-16s |%n",
                    timestamp, action, performer, target);
        }

        System.out.println("+---------------------+------------------+------------------+------------------+");
    }

    /**
     * Сохраняет журнал в файл.
     */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== ASYNC AUDIT LOG ===");
            writer.println("Generated at: " + LocalDateTime.now().format(FORMATTER));
            writer.println("Total entries: " + entries.size());
            writer.println();

            for (AuditEntry entry : entries) {
                writer.println("[" + entry.timestamp() + "]");
                writer.println("  Action:    " + entry.action());
                writer.println("  Performer: " + entry.performer());
                writer.println("  Target:    " + entry.target());
                writer.println("  Details:   " + (entry.details() != null ? entry.details() : "N/A"));
                writer.println();
            }
        }
    }

    /**
     * Размер обработанных записей.
     */
    public int size() {
        return entries.size();
    }

    /**
     * Размер очереди необработанных записей.
     */
    public int pendingQueueSize() {
        return queue.size();
    }

    /**
     * Очищает журнал.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Останавливает worker-поток.
     */
    public void shutdown() {
        running.set(false);
        try {
            workerThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
