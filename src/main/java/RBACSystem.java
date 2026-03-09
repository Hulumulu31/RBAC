import java.util.*;
import java.util.stream.Collectors;

/**
 * Главная система RBAC, содержащая все менеджеры.
 */
public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
    }

    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.currentUser = username;
        }
    }

    public void initialize() {
        // Создаем права доступа
        Permission readUsers = new Permission("READ", "users", "Read users information");
        Permission writeUsers = new Permission("WRITE", "users", "Create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Delete users");
        Permission readRoles = new Permission("READ", "roles", "Read roles information");
        Permission writeRoles = new Permission("WRITE", "roles", "Create and edit roles");
        Permission deleteRoles = new Permission("DELETE", "roles", "Delete roles");
        Permission readAssignments = new Permission("READ", "assignments", "Read assignments information");
        Permission writeAssignments = new Permission("WRITE", "assignments", "Create and edit assignments");
        Permission adminAll = new Permission("ALL", "all", "Full administrative access");

        // Создаем роли
        Role adminRole = new Role("Admin", "Full administrative access to all system functions");
        adminRole.addPermission(adminAll);
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(deleteRoles);
        adminRole.addPermission(readAssignments);
        adminRole.addPermission(writeAssignments);

        Role managerRole = new Role("Manager", "Manage users and view reports");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readRoles);
        managerRole.addPermission(readAssignments);
        managerRole.addPermission(writeAssignments);

        Role viewerRole = new Role("Viewer", "Read-only access to view information");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(readAssignments);

        // Добавляем роли в систему
        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        // Создаем администратора
        User admin = User.validate("admin", "System Administrator", "admin@example.com");
        userManager.add(admin);

        // Назначаем роль Admin администратору
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(admin, adminRole, metadata);
        assignmentManager.add(adminAssignment);
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RBAC System Statistics ===\n\n");
        
        sb.append("Users: ").append(userManager.count()).append("\n");
        sb.append("Roles: ").append(roleManager.count()).append("\n");
        
        int totalAssignments = assignmentManager.count();
        int activeAssignments = (int) assignmentManager.findAll().stream()
            .filter(RoleAssignment::isActive).count();
        int expiredAssignments = totalAssignments - activeAssignments;
        
        sb.append("Assignments: ").append(totalAssignments)
          .append(" (Active: ").append(activeAssignments)
          .append(", Expired: ").append(expiredAssignments).append(")\n");
        
        double avgRolesPerUser = userManager.count() > 0 
            ? (double) activeAssignments / userManager.count() 
            : 0;
        sb.append(String.format("Average roles per user: %.2f\n", avgRolesPerUser));
        
        // Top 3 roles
        sb.append("\nTop 3 most assigned roles:\n");
        Map<String, Long> roleCounts = assignmentManager.getActiveAssignments().stream()
            .collect(Collectors.groupingBy(a -> a.role().getName(), Collectors.counting()));
        
        roleCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .forEach(e -> sb.append("  - ").append(e.getKey())
                .append(": ").append(e.getValue()).append(" assignments\n"));
        
        return sb.toString();
    }
}