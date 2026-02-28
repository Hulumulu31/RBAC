import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Генератор отчётов для системы RBAC.
 * Создаёт различные отчёты по пользователям, ролям и правам доступа.
 */
public class ReportGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Генерирует отчёт по всем пользователям с их ролями.
     *
     * @param userManager менеджер пользователей
     * @param assignmentManager менеджер назначений
     * @return строка с отчётом
     */
    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("================================================================================\n");
        sb.append("                         USER REPORT\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        List<User> users = userManager.findAll();
        
        if (users.isEmpty()) {
            sb.append("No users found.\n");
            return sb.toString();
        }

        // Заголовок таблицы
        sb.append(String.format("%-20s | %-30s | %-35s | %-10s\n", 
                "Username", "Full Name", "Email", "Roles Count"));
        sb.append(String.join("", Collections.nCopies(105, "-"))).append("\n");

        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            long activeRoles = assignments.stream().filter(RoleAssignment::isActive).count();
            
            sb.append(String.format("%-20s | %-30s | %-35s | %-10d\n",
                    FormatUtils.truncate(user.username(), 20),
                    FormatUtils.truncate(user.fullName(), 30),
                    FormatUtils.truncate(user.email(), 35),
                    activeRoles));
        }

        sb.append(String.join("", Collections.nCopies(105, "-"))).append("\n");
        sb.append("Total users: ").append(users.size()).append("\n");

        // Детальная информация по каждому пользователю
        sb.append("\n=== DETAILED USER INFORMATION ===\n\n");
        
        for (User user : users) {
            sb.append("User: ").append(user.username()).append("\n");
            sb.append("  Full Name: ").append(user.fullName()).append("\n");
            sb.append("  Email: ").append(user.email()).append("\n");
            
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .toList();
            
            if (activeAssignments.isEmpty()) {
                sb.append("  Roles: None\n");
            } else {
                sb.append("  Roles:\n");
                for (RoleAssignment assignment : activeAssignments) {
                    sb.append("    - ").append(assignment.role().getName())
                            .append(" (assigned: ").append(assignment.metadata().assignedAt()).append(")\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует отчёт по ролям с количеством пользователей.
     *
     * @param roleManager менеджер ролей
     * @param assignmentManager менеджер назначений
     * @return строка с отчётом
     */
    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("================================================================================\n");
        sb.append("                         ROLE REPORT\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        List<Role> roles = roleManager.findAll();
        
        if (roles.isEmpty()) {
            sb.append("No roles found.\n");
            return sb.toString();
        }

        // Заголовок таблицы
        sb.append(String.format("%-35s | %-10s | %-15s | %-10s\n", 
                "Role Name", "ID", "Permissions", "Users"));
        sb.append(String.join("", Collections.nCopies(80, "-"))).append("\n");

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long userCount = assignments.stream().filter(RoleAssignment::isActive).count();
            
            sb.append(String.format("%-35s | %-10s | %-15d | %-10d\n",
                    FormatUtils.truncate(role.getName(), 35),
                    FormatUtils.truncate(role.getId(), 10),
                    role.getPermissions().size(),
                    userCount));
        }

        sb.append(String.join("", Collections.nCopies(80, "-"))).append("\n");
        sb.append("Total roles: ").append(roles.size()).append("\n");

        // Детальная информация по каждой роли
        sb.append("\n=== DETAILED ROLE INFORMATION ===\n\n");
        
        for (Role role : roles) {
            sb.append("Role: ").append(role.getName()).append("\n");
            sb.append("  ID: ").append(role.getId()).append("\n");
            sb.append("  Description: ").append(role.getDescription()).append("\n");
            
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .toList();
            
            sb.append("  Users with this role: ").append(activeAssignments.size()).append("\n");
            if (!activeAssignments.isEmpty()) {
                for (RoleAssignment assignment : activeAssignments) {
                    sb.append("    - ").append(assignment.user().username()).append("\n");
                }
            }
            
            sb.append("  Permissions:\n");
            if (role.getPermissions().isEmpty()) {
                sb.append("    None\n");
            } else {
                for (Permission perm : role.getPermissions()) {
                    sb.append("    - ").append(perm.format()).append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует матрицу прав доступа (пользователи × ресурсы).
     *
     * @param userManager менеджер пользователей
     * @param assignmentManager менеджер назначений
     * @return строка с матрицей прав
     */
    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("================================================================================\n");
        sb.append("                      PERMISSION MATRIX\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        List<User> users = userManager.findAll();
        
        if (users.isEmpty()) {
            sb.append("No users found.\n");
            return sb.toString();
        }

        // Собираем все уникальные ресурсы и права
        Set<String> allResources = new TreeSet<>();
        Set<String> allPermissions = new TreeSet<>();
        
        for (User user : users) {
            Set<Permission> userPermissions = assignmentManager.getUserPermissions(user);
            for (Permission perm : userPermissions) {
                allResources.add(perm.resource());
                allPermissions.add(perm.name() + ":" + perm.resource());
            }
        }

        if (allPermissions.isEmpty()) {
            sb.append("No permissions assigned to any users.\n");
            return sb.toString();
        }

        // Формируем заголовок с ресурсами
        sb.append(String.format("%-20s | ", "Username"));
        for (String resource : allResources) {
            sb.append(String.format("%-15s | ", FormatUtils.truncate(resource, 15)));
        }
        sb.append("\n");
        
        // Разделитель
        sb.append(String.join("", Collections.nCopies(22 + allResources.size() * 17, "-"))).append("\n");

        // Строки матрицы
        for (User user : users) {
            Set<Permission> userPermissions = assignmentManager.getUserPermissions(user);
            Set<String> userPermSet = new HashSet<>();
            for (Permission perm : userPermissions) {
                userPermSet.add(perm.name() + ":" + perm.resource());
            }

            sb.append(String.format("%-20s | ", FormatUtils.truncate(user.username(), 20)));
            
            for (String resource : allResources) {
                // Проверяем, есть ли у пользователя какие-либо права на этот ресурс
                boolean hasAnyPermission = userPermSet.stream()
                        .anyMatch(p -> p.endsWith(":" + resource));
                
                String cell = hasAnyPermission ? "X" : "-";
                sb.append(String.format("%-15s | ", cell));
            }
            sb.append("\n");
        }

        sb.append(String.join("", Collections.nCopies(22 + allResources.size() * 17, "-"))).append("\n");
        
        // Детализация по правам
        sb.append("\n=== PERMISSION LEGEND ===\n");
        sb.append("X = User has at least one permission on this resource\n");
        sb.append("- = User has no permissions on this resource\n\n");

        // Список всех прав по пользователям
        sb.append("=== DETAILED PERMISSIONS BY USER ===\n\n");
        
        for (User user : users) {
            Set<Permission> userPermissions = assignmentManager.getUserPermissions(user);
            sb.append(user.username()).append(":\n");
            
            if (userPermissions.isEmpty()) {
                sb.append("  No permissions\n");
            } else {
                // Группируем права по ресурсам
                Map<String, List<String>> resourcePermissions = new TreeMap<>();
                for (Permission perm : userPermissions) {
                    resourcePermissions
                        .computeIfAbsent(perm.resource(), k -> new ArrayList<>())
                        .add(perm.name());
                }
                
                for (Map.Entry<String, List<String>> entry : resourcePermissions.entrySet()) {
                    sb.append("  ").append(entry.getKey()).append(": ")
                            .append(String.join(", ", entry.getValue())).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Экспортирует отчёт в файл.
     *
     * @param report текст отчёта
     * @param filename имя файла для сохранения
     * @throws IOException если произошла ошибка записи
     */
    public void exportToFile(String report, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
        }
    }

    /**
     * Генерирует сводный отчёт по системе.
     *
     * @param userManager менеджер пользователей
     * @param roleManager менеджер ролей
     * @param assignmentManager менеджер назначений
     * @return строка с сводным отчётом
     */
    public String generateSummaryReport(UserManager userManager, RoleManager roleManager, 
                                        AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("================================================================================\n");
        sb.append("                         SYSTEM SUMMARY REPORT\n");
        sb.append("                    Generated: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        sb.append("================================================================================\n\n");

        sb.append("OVERVIEW\n");
        sb.append(String.join("", Collections.nCopies(80, "-"))).append("\n");
        sb.append(String.format("%-30s: %d\n", "Total Users", userManager.count()));
        sb.append(String.format("%-30s: %d\n", "Total Roles", roleManager.count()));
        sb.append(String.format("%-30s: %d\n", "Total Assignments", assignmentManager.count()));
        
        long activeAssignments = assignmentManager.getActiveAssignments().size();
        long expiredAssignments = assignmentManager.getExpiredAssignments().size();
        
        sb.append(String.format("%-30s: %d\n", "Active Assignments", activeAssignments));
        sb.append(String.format("%-30s: %d\n", "Expired/Revoked Assignments", expiredAssignments));
        sb.append("\n");

        // Статистика по типам назначений
        long permanentCount = assignmentManager.findAll().stream()
                .filter(a -> a.assignmentType().equals("PERMANENT"))
                .count();
        long temporaryCount = assignmentManager.findAll().stream()
                .filter(a -> a.assignmentType().equals("TEMPORARY"))
                .count();
        
        sb.append("ASSIGNMENT TYPES\n");
        sb.append(String.join("", Collections.nCopies(80, "-"))).append("\n");
        sb.append(String.format("%-30s: %d\n", "Permanent Assignments", permanentCount));
        sb.append(String.format("%-30s: %d\n", "Temporary Assignments", temporaryCount));
        sb.append("\n");

        // Топ ролей по количеству пользователей
        sb.append("TOP ROLES BY USER COUNT\n");
        sb.append(String.join("", Collections.nCopies(80, "-"))).append("\n");
        
        List<Role> roles = roleManager.findAll();
        roles.sort((r1, r2) -> {
            long count1 = assignmentManager.findByRole(r1).stream().filter(RoleAssignment::isActive).count();
            long count2 = assignmentManager.findByRole(r2).stream().filter(RoleAssignment::isActive).count();
            return Long.compare(count2, count1);
        });
        
        int rank = 1;
        for (Role role : roles) {
            if (rank > 5) break;
            long userCount = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive).count();
            sb.append(String.format("%d. %-25s: %d users\n", rank++, role.getName(), userCount));
        }

        return sb.toString();
    }
}
