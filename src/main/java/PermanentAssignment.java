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
}