public class PermanentAssignment extends AbstractRoleAssignment {
    private boolean revoked = false;
    
    // Конструктор
    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }
    
    // Реализация абстрактных методов
    @Override
    public boolean isActive() {
        return !revoked;
    }
    
    @Override
    public String assignmentType() {
        return "PERMANENT";
    }
    
    // Методы для управления статусом отзыва
    public void revoke() {
        this.revoked = true;
    }
    
    public boolean isRevoked() {
        return revoked;
    }
    
    public static void main(String[] args) {
        try {
            // Создание тестовых объектов
            User user = User.validate("john_doe", "John Doe", "john@example.com");
            Role adminRole = new Role("Administrator", "Full system access");
            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Initial setup");
            
            // Создание постоянного назначения
            PermanentAssignment permAssign = new PermanentAssignment(user, adminRole, metadata);
            
            System.out.println("Permanent assignment created:");
            System.out.println(permAssign.summary());
            System.out.println("Is active: " + permAssign.isActive());
            System.out.println("Is revoked: " + permAssign.isRevoked());
            
            // Отзыв назначения
            permAssign.revoke();
            System.out.println("\nAfter revocation:");
            System.out.println("Is active: " + permAssign.isActive());
            System.out.println("Is revoked: " + permAssign.isRevoked());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}