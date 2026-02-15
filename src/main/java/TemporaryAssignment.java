import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;
    
    // Конструктор
    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt) {
        super(user, role, metadata);
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiration date cannot be null or empty");
        }
        this.expiresAt = expiresAt;
        this.autoRenew = false;
    }
    
    // Реализация абстрактных методов
    @Override
    public boolean isActive() {
        // Сравнение текущей даты с датой истечения
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try {
            LocalDateTime expiration = LocalDateTime.parse(this.expiresAt, formatter);
            boolean notExpired = now.isBefore(expiration) || now.isEqual(expiration);
            
            // Если срок действия истек и включено автопродление, продляем на 1 день
            if (!notExpired && autoRenew) {
                extend(addDaysToDate(this.expiresAt, 1));
                return true; // После продления назначение снова активно
            }
            
            return notExpired;
        } catch (Exception e) {
            // Если формат даты неверный, считаем назначение неактивным
            System.err.println("Invalid date format: " + this.expiresAt + ", Error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }
    
    // Методы для управления временными аспектами назначения
    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("New expiration date cannot be null or empty");
        }
        this.expiresAt = newExpirationDate;
    }
    
    public boolean isExpired() {
        return !isActive();
    }
    
    public String getTimeRemaining() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try {
            LocalDateTime expiration = LocalDateTime.parse(this.expiresAt, formatter);
            
            if (now.isAfter(expiration)) {
                return "EXPIRED";
            }
            
            long days = ChronoUnit.DAYS.between(now, expiration);
            long hours = ChronoUnit.HOURS.between(now, expiration) % 24;
            
            return days + " days, " + hours + " hours remaining";
        } catch (Exception e) {
            return "Invalid date format";
        }
    }
    
    // Метод для добавления дней к дате (для автопродления)
    private String addDaysToDate(String dateStr, int daysToAdd) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
        date = date.plusDays(daysToAdd);
        return date.format(formatter);
    }
    
    // Переопределение метода summary с информацией о дате истечения
    @Override
    public String summary() {
        String baseSummary = super.summary();
        return baseSummary + "\nExpires at: " + expiresAt + 
               "\nAuto-renew: " + (autoRenew ? "ON" : "OFF") +
               "\nTime remaining: " + getTimeRemaining();
    }
    
    // Методы для управления автопродлением
    public void enableAutoRenew() {
        this.autoRenew = true;
    }
    
    public void disableAutoRenew() {
        this.autoRenew = false;
    }
    
    public boolean isAutoRenewEnabled() {
        return this.autoRenew;
    }
    
    public static void main(String[] args) {
        try {
            // Создание тестовых объектов
            User user = User.validate("jane_doe", "Jane Doe", "jane@example.com");
            Role viewerRole = new Role("Viewer", "Limited read access");
            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Temporary access for project");
            
            // Создание временного назначения (истекает через 1 минуту)
            String futureDate = LocalDateTime.now().plusMinutes(1).format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            TemporaryAssignment tempAssign = new TemporaryAssignment(user, viewerRole, metadata, futureDate);
            
            System.out.println("Temporary assignment created:");
            System.out.println(tempAssign.summary());
            System.out.println("Is active: " + tempAssign.isActive());
            System.out.println("Is expired: " + tempAssign.isExpired());
            
            // Продление назначения
            String extendedDate = LocalDateTime.now().plusHours(1).format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            tempAssign.extend(extendedDate);
            System.out.println("\nAfter extension:");
            System.out.println("New expiration: " + tempAssign.getTimeRemaining());
            
            // Тестирование автопродления
            tempAssign.enableAutoRenew();
            System.out.println("\nAuto-renew enabled: " + tempAssign.isAutoRenewEnabled());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}