import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    
    // Статический метод для создания метаданных с текущей датой/временем
    public static AssignmentMetadata now(String assignedBy, String reason) {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned by cannot be null or empty");
        }
        
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new AssignmentMetadata(assignedBy, currentTime, reason);
    }
    
    // Конструктор record автоматически устанавливает значения
    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned by cannot be null or empty");
        }
        
        if (assignedAt == null || assignedAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned at cannot be null or empty");
        }
    }
    
    // Метод для форматированного вывода
    public String format() {
        return "Assigned by: " + assignedBy +
               " at " + assignedAt +
               (reason != null && !reason.trim().isEmpty() ? " (Reason: " + reason + ")" : "");
    }
}