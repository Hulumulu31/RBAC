import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.UUID;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;
    
    // Счетчик для генерации ID (альтернативный способ, если не используем UUID)
    private static int counter = 0;
    
    // Конструктор для создания новой роли
    public Role(String name, String description) {
        // Генерация ID с использованием UUID
        this.id = "role_" + UUID.randomUUID().toString();
        
        // Проверка, что name не null и не пустое
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        
        // Проверка, что description не null и не пустое
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Role description cannot be null or empty");
        }
        
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }
    
    // Методы для управления правами
    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }
    
    public void removePermission(Permission permission) {
        if (permission != null) {
            permissions.remove(permission);
        }
    }
    
    public boolean hasPermission(Permission permission) {
        return permission != null && permissions.contains(permission);
    }
    
    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || permissionName.trim().isEmpty() || 
            resource == null || resource.trim().isEmpty()) {
            return false;
        }
        
        for (Permission perm : permissions) {
            if (perm.name().equalsIgnoreCase(permissionName) && 
                perm.resource().equalsIgnoreCase(resource)) {
                return true;
            }
        }
        return false;
    }
    
    // Метод для получения неизменяемой копии прав
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }
    
    // Геттеры
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Переопределение equals и hashCode по полю id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return id.equals(role.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    // Переопределение toString для читаемого вывода
    @Override
    public String toString() {
        return "Role{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", permissionsCount=" + permissions.size() +
                '}';
    }
    
    // Метод для форматированного вывода
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");

        for (Permission perm : permissions) {
            sb.append(" - ").append(perm.format()).append("\n");
        }

        return sb.toString();
    }
}