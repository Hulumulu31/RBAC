import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    protected final String assignmentId;
    protected final User user;
    protected final Role role;
    protected final AssignmentMetadata metadata;
    
    // Конструктор, принимающий User, Role и AssignmentMetadata
    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        
        this.assignmentId = "assign_" + UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }
    
    // Реализация методов интерфейса
    @Override
    public String assignmentId() {
        return assignmentId;
    }
    
    @Override
    public User user() {
        return user;
    }
    
    @Override
    public Role role() {
        return role;
    }
    
    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }
    
    // Абстрактные методы, которые должны быть реализованы в подклассах
    @Override
    public abstract boolean isActive();
    
    @Override
    public abstract String assignmentType();
    
    // Переопределение equals и hashCode по assignmentId
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) obj;
        return assignmentId.equals(that.assignmentId);
    }
    
    @Override
    public int hashCode() {
        return assignmentId.hashCode();
    }
    
    // Метод для получения сводки назначения
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
          .append(assignmentType())
          .append("] ")
          .append(role.getName())
          .append(" assigned to ")
          .append(user.username())
          .append(" by ")
          .append(metadata.assignedBy())
          .append(" at ")
          .append(metadata.assignedAt());
          
        if (metadata.reason() != null && !metadata.reason().trim().isEmpty()) {
            sb.append("\nReason: ").append(metadata.reason());
        }
        
        sb.append("\nStatus: ").append(isActive() ? "ACTIVE" : "INACTIVE");
        
        return sb.toString();
    }
}