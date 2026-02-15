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
    
    public static void main(String[] args) {
        try {
            // Тестирование создания метаданных
            AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Initial setup");
            System.out.println("Created metadata: " + metadata1.format());
            
            // Тестирование создания с заданной датой
            AssignmentMetadata metadata2 = new AssignmentMetadata("manager", "2026-02-07 15:00:00", "Project access");
            System.out.println("Custom metadata: " + metadata2.format());
            
            // Тестирование ошибок
            try {
                new AssignmentMetadata("", "2026-02-07 15:00:00", "test");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for empty assignedBy: " + e.getMessage());
            }
            
            try {
                new AssignmentMetadata(null, "2026-02-07 15:00:00", "test");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for null assignedBy: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}