import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Система логирования событий (Audit Log).
 * Записывает все важные действия в системе для последующего аудита.
 */
public class AuditLog {
    private final List<AuditEntry> entries;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Запись аудита.
     *
     * @param timestamp временная метка
     * @param action действие
     * @param performer исполнитель (пользователь, выполнивший действие)
     * @param target цель действия (объект, над которым выполнено действие)
     * @param details дополнительные детали
     */
    public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
    ) {}

    /**
     * Создает новый пустой журнал аудита.
     */
    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    /**
     * Записывает событие в журнал аудита.
     *
     * @param action действие (например, "CREATE_USER", "DELETE_ROLE")
     * @param performer исполнитель действия (имя пользователя)
     * @param target цель действия (имя объекта)
     * @param details дополнительные детали
     */
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        entries.add(new AuditEntry(timestamp, action, performer, target, details));
    }

    /**
     * Возвращает все записи журнала.
     *
     * @return список всех записей
     */
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    /**
     * Возвращает записи, выполненные указанным пользователем.
     *
     * @param performer имя исполнителя
     * @return список записей, отфильтрованных по исполнителю
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
     * Возвращает записи с указанным типом действия.
     *
     * @param action тип действия
     * @return список записей, отфильтрованных по действию
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
     * Возвращает записи за указанный период.
     *
     * @param startDate начальная дата (в формате yyyy-MM-dd HH:mm:ss)
     * @param endDate конечная дата (в формате yyyy-MM-dd HH:mm:ss)
     * @return список записей за указанный период
     */
    public List<AuditEntry> getByDateRange(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }
        return entries.stream()
                .filter(entry -> {
                    String ts = entry.timestamp();
                    return ts.compareTo(startDate) >= 0 && ts.compareTo(endDate) <= 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * Выводит все записи журнала в консоль в отформатированном виде.
     */
    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Audit log is empty.");
            return;
        }

        System.out.println("\n=== AUDIT LOG ===");
        System.out.println("Total entries: " + entries.size());
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
     * Сохраняет журнал аудита в файл.
     *
     * @param filename имя файла для сохранения
     * @throws IOException если произошла ошибка записи
     */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== AUDIT LOG ===");
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
     * Возвращает количество записей в журнале.
     *
     * @return количество записей
     */
    public int size() {
        return entries.size();
    }

    /**
     * Очищает журнал аудита.
     */
    public void clear() {
        entries.clear();
    }
}
