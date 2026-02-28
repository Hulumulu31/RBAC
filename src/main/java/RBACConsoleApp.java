import java.util.*;
import java.util.stream.Collectors;

/**
 * Интерактивная консольная утилита для управления пользователями, ролями и правами доступа
 * с использованием модели RBAC (Role-Based Access Control).
 */
public class RBACConsoleApp {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private AuditLog auditLog;
    private ReportGenerator reportGenerator;
    private Scanner scanner;
    private String currentUser; // Текущий пользователь (для аудита)

    public RBACConsoleApp() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.auditLog = new AuditLog();
        this.reportGenerator = new ReportGenerator();
        this.scanner = new Scanner(System.in);
        this.currentUser = "system";
    }

    public void start() {
        System.out.println(FormatUtils.formatHeader("RBAC Console Application"));
        System.out.println("Welcome to the Role-Based Access Control system!");
        System.out.println("Type 'help' for commands list.\n");

        // Создадим тестовые данные для демонстрации
        createDemoData();

        boolean running = true;
        while (running) {
            printMainMenu();
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0];
            String args = parts.length > 1 ? parts[1] : "";

            // Проверяем команды выхода
            if (command.equals("exit") || command.equals("quit")) {
                System.out.println(FormatUtils.ANSI_CYAN + "Exiting RBAC Console Application... Goodbye!" + FormatUtils.ANSI_RESET);
                running = false;
                break;
            }

            if (!handleCommand(command, args)) {
                System.out.println(FormatUtils.ANSI_RED + "Unknown command: " + command + ". Type 'help' for available commands." + FormatUtils.ANSI_RESET);
            }
        }
        
        scanner.close();
    }

    private boolean handleCommand(String command, String args) {
        switch (command) {
            case "help":
                printHelp();
                return true;
            case "users":
            case "list-users":
                listAllUsers();
                return true;
            case "create-user":
                createUserWizard();
                return true;
            case "edit-user":
                editUserWizard();
                return true;
            case "delete-user":
                deleteUserWizard();
                return true;
            case "roles":
            case "list-roles":
                listAllRoles();
                return true;
            case "create-role":
                createRoleWizard();
                return true;
            case "delete-role":
                deleteRoleWizard();
                return true;
            case "view-role":
                viewRoleWizard();
                return true;
            case "assign-role":
                assignRoleWizard();
                return true;
            case "revoke":
                revokeAssignmentWizard();
                return true;
            case "assignments":
            case "list-assignments":
                listAllAssignments();
                return true;
            case "report-users":
                generateUserReport();
                return true;
            case "report-roles":
                generateRoleReport();
                return true;
            case "report-matrix":
                generatePermissionMatrix();
                return true;
            case "report-summary":
                generateSummaryReport();
                return true;
            case "audit-log":
                viewAuditLog();
                return true;
            case "audit-save":
                saveAuditLog();
                return true;
            case "user":
                viewUserWizard(args);
                return true;
            case "set-user":
                setCurrentUser(args);
                return true;
            case "clear":
                clearConsole();
                return true;
            default:
                return false;
        }
    }

    private void printHelp() {
        System.out.println("\n" + FormatUtils.formatHeader("Available Commands"));
        
        String[] headers = {"Command", "Description"};
        List<String[]> rows = Arrays.asList(
            new String[]{"help", "Show this help message"},
            new String[]{"users / list-users", "List all users"},
            new String[]{"create-user", "Create new user (wizard)"},
            new String[]{"edit-user", "Edit existing user (wizard)"},
            new String[]{"delete-user", "Delete user (wizard)"},
            new String[]{"user <username>", "View user details"},
            new String[]{"roles / list-roles", "List all roles"},
            new String[]{"create-role", "Create new role (wizard)"},
            new String[]{"delete-role", "Delete role (wizard)"},
            new String[]{"view-role", "View role details (wizard)"},
            new String[]{"assign-role", "Assign role to user (wizard)"},
            new String[]{"revoke", "Revoke assignment (wizard)"},
            new String[]{"assignments", "List all assignments"},
            new String[]{"report-users", "Generate user report"},
            new String[]{"report-roles", "Generate role report"},
            new String[]{"report-matrix", "Generate permission matrix"},
            new String[]{"report-summary", "Generate summary report"},
            new String[]{"audit-log", "View audit log"},
            new String[]{"audit-save", "Save audit log to file"},
            new String[]{"set-user <name>", "Set current user for audit"},
            new String[]{"clear", "Clear console"},
            new String[]{"exit / quit", "Exit application"}
        );

        System.out.println(FormatUtils.formatTable(headers, rows));
    }

    private void printMainMenu() {
        System.out.println("\n" + FormatUtils.ANSI_CYAN + "┌────────────────────────────────────────────────────────────┐" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET + "  RBAC System - Main Menu                           " + FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "├────────────────────────────────────────────────────────────┤" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET + "  Current user: " + FormatUtils.padRight(currentUser, 32) + "  " + FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "└────────────────────────────────────────────────────────────┘" + FormatUtils.ANSI_RESET);
        System.out.print("\nEnter command (or 'help'): ");
    }

    // ==================== USER COMMANDS ====================

    private void createUserWizard() {
        System.out.println(FormatUtils.formatSubHeader("Create New User"));
        
        String username = ConsoleUtils.promptUsername(scanner, "Enter username: ", true);
        
        // Проверка на дубликат
        if (userManager.findByUsername(username).isPresent()) {
            ConsoleUtils.printError("User with username '" + username + "' already exists");
            return;
        }

        String fullName = ConsoleUtils.promptString(scanner, "Enter full name: ", true);
        String email = ConsoleUtils.promptEmail(scanner, "Enter email: ", true);

        try {
            User user = User.validate(username, fullName, email);
            userManager.add(user);
            auditLog.log("CREATE_USER", currentUser, username, "Full name: " + fullName + ", Email: " + email);
            ConsoleUtils.printSuccess("User created successfully: " + user.format());
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError("Error creating user: " + e.getMessage());
        }
    }

    private void editUserWizard() {
        System.out.println(FormatUtils.formatSubHeader("Edit User"));
        
        String username = ConsoleUtils.promptString(scanner, "Enter username to edit: ", true);
        
        User existingUser = userManager.findByUsername(username).orElse(null);
        if (existingUser == null) {
            ConsoleUtils.printError("User not found: " + username);
            return;
        }

        System.out.println("Current user info: " + existingUser.format());
        
        String newFullName = ConsoleUtils.promptString(scanner, 
            "Enter new full name (or press Enter to keep current): ", false);
        if (newFullName.isEmpty()) {
            newFullName = existingUser.fullName();
        }

        String newEmail = ConsoleUtils.promptString(scanner, 
            "Enter new email (or press Enter to keep current): ", false);
        if (newEmail.isEmpty()) {
            newEmail = existingUser.email();
        }

        try {
            userManager.update(username, newFullName, newEmail);
            User updatedUser = userManager.findByUsername(username).orElse(null);
            auditLog.log("EDIT_USER", currentUser, username, 
                "New full name: " + newFullName + ", New email: " + newEmail);
            ConsoleUtils.printSuccess("User updated successfully: " + updatedUser.format());
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError("Error updating user: " + e.getMessage());
        }
    }

    private void deleteUserWizard() {
        System.out.println(FormatUtils.formatSubHeader("Delete User"));
        
        String username = ConsoleUtils.promptString(scanner, "Enter username to delete: ", true);

        User userToRemove = userManager.findByUsername(username).orElse(null);
        if (userToRemove == null) {
            ConsoleUtils.printError("User not found: " + username);
            return;
        }

        // Проверка назначений
        List<RoleAssignment> userAssignments = assignmentManager.findByUser(userToRemove);
        long activeAssignments = userAssignments.stream().filter(RoleAssignment::isActive).count();
        
        if (activeAssignments > 0) {
            ConsoleUtils.printWarning("This user has " + activeAssignments + " active role assignment(s).");
        }

        if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to delete user '" + username + "'")) {
            // Удаляем назначения
            for (RoleAssignment assignment : userAssignments) {
                assignmentManager.remove(assignment);
            }

            userManager.remove(userToRemove);
            auditLog.log("DELETE_USER", currentUser, username, 
                "Removed " + userAssignments.size() + " assignment(s)");
            ConsoleUtils.printSuccess("User deleted successfully");
        }
    }

    private void viewUserWizard(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = ConsoleUtils.promptString(scanner, "Enter username to view: ", true);
        }
        
        User user = userManager.findByUsername(username.trim()).orElse(null);
        if (user != null) {
            System.out.println(FormatUtils.formatBox(
                "User: " + user.username() + "\n" +
                "Full Name: " + user.fullName() + "\n" +
                "Email: " + user.email()
            ));
            
            // Показываем роли пользователя
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());
            
            if (!activeAssignments.isEmpty()) {
                System.out.println("\nActive Roles:");
                for (RoleAssignment assignment : activeAssignments) {
                    System.out.println("  - " + assignment.role().getName() + 
                        " (assigned: " + assignment.metadata().assignedAt() + ")");
                }
            } else {
                System.out.println("\nNo active roles assigned");
            }
        } else {
            ConsoleUtils.printError("User not found: " + username);
        }
    }

    private void listAllUsers() {
        List<User> allUsers = userManager.findAll();
        if (allUsers.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println(FormatUtils.formatSubHeader("All Users"));
        
        String[] headers = {"Username", "Full Name", "Email", "Active Roles"};
        List<String[]> rows = new ArrayList<>();
        
        for (User user : allUsers) {
            long activeRoles = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .count();
            rows.add(new String[]{
                user.username(),
                user.fullName(),
                user.email(),
                String.valueOf(activeRoles)
            });
        }
        
        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.println("Total users: " + userManager.count());
    }

    // ==================== ROLE COMMANDS ====================

    private void createRoleWizard() {
        System.out.println(FormatUtils.formatSubHeader("Create New Role"));
        
        String roleName = ConsoleUtils.promptString(scanner, "Enter role name: ", true);
        
        // Проверка на дубликат
        if (roleManager.findByName(roleName).isPresent()) {
            ConsoleUtils.printError("Role with name '" + roleName + "' already exists");
            return;
        }

        String description = ConsoleUtils.promptString(scanner, "Enter role description: ", true);

        try {
            Role role = new Role(roleName, description);
            roleManager.add(role);
            auditLog.log("CREATE_ROLE", currentUser, roleName, description);
            ConsoleUtils.printSuccess("Role created successfully: " + role.getName() + 
                " (ID: " + role.getId() + ")");
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError("Error creating role: " + e.getMessage());
        }
    }

    private void deleteRoleWizard() {
        System.out.println(FormatUtils.formatSubHeader("Delete Role"));
        
        String roleId = ConsoleUtils.promptString(scanner, "Enter role ID to delete: ", true);

        Role roleToRemove = roleManager.findById(roleId).orElse(null);
        if (roleToRemove == null) {
            ConsoleUtils.printError("Role not found: " + roleId);
            return;
        }

        // Проверка назначений
        List<RoleAssignment> roleAssignments = assignmentManager.findByRole(roleToRemove);
        long activeAssignments = roleAssignments.stream().filter(RoleAssignment::isActive).count();
        
        if (activeAssignments > 0) {
            ConsoleUtils.printWarning("This role is assigned to " + activeAssignments + " user(s).");
        }

        if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to delete role '" + roleToRemove.getName() + "'")) {
            // Удаляем назначения
            for (RoleAssignment assignment : roleAssignments) {
                assignmentManager.remove(assignment);
            }

            roleManager.remove(roleToRemove);
            auditLog.log("DELETE_ROLE", currentUser, roleToRemove.getName(), 
                "Removed " + roleAssignments.size() + " assignment(s)");
            ConsoleUtils.printSuccess("Role deleted successfully");
        }
    }

    private void viewRoleWizard() {
        System.out.println(FormatUtils.formatSubHeader("View Role"));
        
        String roleId = ConsoleUtils.promptString(scanner, "Enter role ID to view: ", true);

        Role role = roleManager.findById(roleId).orElse(null);
        if (role != null) {
            System.out.println(role.format());
            
            // Показываем пользователей с этой ролью
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());
            
            if (!activeAssignments.isEmpty()) {
                System.out.println("Users with this role:");
                for (RoleAssignment assignment : activeAssignments) {
                    System.out.println("  - " + assignment.user().username());
                }
            } else {
                System.out.println("No users have this role");
            }
        } else {
            ConsoleUtils.printError("Role not found: " + roleId);
        }
    }

    private void listAllRoles() {
        List<Role> allRoles = roleManager.findAll();
        if (allRoles.isEmpty()) {
            System.out.println("No roles found.");
            return;
        }

        System.out.println(FormatUtils.formatSubHeader("All Roles"));
        
        String[] headers = {"Role Name", "ID", "Permissions", "Users"};
        List<String[]> rows = new ArrayList<>();
        
        for (Role role : allRoles) {
            long userCount = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive)
                    .count();
            rows.add(new String[]{
                role.getName(),
                role.getId().substring(0, Math.min(8, role.getId().length())) + "...",
                String.valueOf(role.getPermissions().size()),
                String.valueOf(userCount)
            });
        }
        
        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.println("Total roles: " + roleManager.count());
    }

    // ==================== ASSIGNMENT COMMANDS ====================

    private void assignRoleWizard() {
        System.out.println(FormatUtils.formatSubHeader("Assign Role to User"));
        
        // Выбор пользователя
        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            ConsoleUtils.printError("No users available. Create a user first.");
            return;
        }
        
        User user = ConsoleUtils.promptChoice(scanner, "Select user:", users, 
            u -> u.username() + " (" + u.fullName() + ")");

        // Выбор роли
        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            ConsoleUtils.printError("No roles available. Create a role first.");
            return;
        }
        
        Role role = ConsoleUtils.promptChoice(scanner, "Select role:", roles, 
            Role::getName);

        // Проверка на дубликат назначения
        if (assignmentManager.findByUser(user).stream()
                .anyMatch(a -> a.role().equals(role) && a.isActive())) {
            ConsoleUtils.printError("User already has this role assigned");
            return;
        }

        String assignedBy = ConsoleUtils.promptString(scanner, 
            "Enter your username (assigner): ", true);
        String reason = ConsoleUtils.promptString(scanner, 
            "Enter reason (optional): ", false);

        AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);

        // Тип назначения
        System.out.println("\nAssignment type:");
        System.out.println("  [1] Permanent");
        System.out.println("  [2] Temporary");
        int typeChoice = ConsoleUtils.promptInt(scanner, "Choose type (1-2): ", 1, 2);

        try {
            if (typeChoice == 1) {
                // Permanent assignment
                var assignment = new PermanentAssignment(user, role, metadata);
                assignmentManager.add(assignment);
                auditLog.log("ASSIGN_ROLE", currentUser, user.username(), 
                    "Role: " + role.getName() + ", Type: PERMANENT");
                ConsoleUtils.printSuccess("Permanent assignment created: " + assignment.summary());
            } else {
                // Temporary assignment
                String expiresAt = ConsoleUtils.promptDate(scanner, 
                    "Enter expiration date (yyyy-MM-dd HH:mm:ss): ", true);
                
                var assignment = new TemporaryAssignment(user, role, metadata, expiresAt);
                assignmentManager.add(assignment);
                auditLog.log("ASSIGN_ROLE", currentUser, user.username(), 
                    "Role: " + role.getName() + ", Type: TEMPORARY, Expires: " + expiresAt);
                ConsoleUtils.printSuccess("Temporary assignment created: " + assignment.summary());
            }
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printError("Error creating assignment: " + e.getMessage());
        }
    }

    private void revokeAssignmentWizard() {
        System.out.println(FormatUtils.formatSubHeader("Revoke Assignment"));
        
        List<RoleAssignment> assignments = assignmentManager.getActiveAssignments();
        if (assignments.isEmpty()) {
            ConsoleUtils.printError("No active assignments found");
            return;
        }

        RoleAssignment assignment = ConsoleUtils.promptChoice(scanner, 
            "Select assignment to revoke:", assignments,
            a -> a.role().getName() + " -> " + a.user().username());

        if (!(assignment instanceof PermanentAssignment)) {
            ConsoleUtils.printError("Only permanent assignments can be revoked");
            return;
        }

        if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to revoke this assignment")) {
            try {
                assignmentManager.revokeAssignment(assignment.assignmentId());
                auditLog.log("REVOKE_ROLE", currentUser, assignment.user().username(), 
                    "Role: " + assignment.role().getName());
                ConsoleUtils.printSuccess("Assignment revoked successfully");
            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError("Error revoking assignment: " + e.getMessage());
            }
        }
    }

    private void listAllAssignments() {
        List<RoleAssignment> allAssignments = assignmentManager.findAll();
        if (allAssignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        System.out.println(FormatUtils.formatSubHeader("All Assignments"));
        
        String[] headers = {"Type", "User", "Role", "Status", "Assigned At"};
        List<String[]> rows = new ArrayList<>();
        
        for (RoleAssignment assignment : allAssignments) {
            String status = assignment.isActive() ? "ACTIVE" : "INACTIVE";
            rows.add(new String[]{
                assignment.assignmentType(),
                assignment.user().username(),
                assignment.role().getName(),
                status,
                assignment.metadata().assignedAt().substring(0, 10)
            });
        }
        
        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.println("Total assignments: " + assignmentManager.count());
    }

    // ==================== REPORT COMMANDS ====================

    private void generateUserReport() {
        System.out.println(FormatUtils.formatSubHeader("User Report"));
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        System.out.println(report);
    }

    private void generateRoleReport() {
        System.out.println(FormatUtils.formatSubHeader("Role Report"));
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        System.out.println(report);
    }

    private void generatePermissionMatrix() {
        System.out.println(FormatUtils.formatSubHeader("Permission Matrix"));
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        System.out.println(report);
    }

    private void generateSummaryReport() {
        System.out.println(FormatUtils.formatSubHeader("Summary Report"));
        String report = reportGenerator.generateSummaryReport(userManager, roleManager, assignmentManager);
        System.out.println(report);
    }

    // ==================== AUDIT LOG COMMANDS ====================

    private void viewAuditLog() {
        System.out.println(FormatUtils.formatSubHeader("Audit Log"));
        
        if (auditLog.size() == 0) {
            System.out.println("Audit log is empty.");
            return;
        }

        System.out.println("\nFilter options:");
        System.out.println("  [1] Show all");
        System.out.println("  [2] Filter by performer");
        System.out.println("  [3] Filter by action");
        int choice = ConsoleUtils.promptInt(scanner, "Choose option (1-3): ", 1, 3);

        List<AuditLog.AuditEntry> entries;
        switch (choice) {
            case 1:
                entries = auditLog.getAll();
                break;
            case 2:
                String performer = ConsoleUtils.promptString(scanner, "Enter performer name: ", true);
                entries = auditLog.getByPerformer(performer);
                break;
            case 3:
                String action = ConsoleUtils.promptString(scanner, "Enter action type: ", true);
                entries = auditLog.getByAction(action);
                break;
            default:
                entries = auditLog.getAll();
        }

        if (entries.isEmpty()) {
            System.out.println("No entries found.");
            return;
        }

        String[] headers = {"Timestamp", "Action", "Performer", "Target"};
        List<String[]> rows = new ArrayList<>();
        
        for (AuditLog.AuditEntry entry : entries) {
            rows.add(new String[]{
                entry.timestamp(),
                entry.action(),
                entry.performer(),
                entry.target()
            });
        }
        
        System.out.println("\n" + FormatUtils.formatTable(headers, rows));
        System.out.println("Total entries: " + entries.size());
    }

    private void saveAuditLog() {
        String filename = ConsoleUtils.promptString(scanner, "Enter filename to save audit log: ", true);
        try {
            auditLog.saveToFile(filename);
            ConsoleUtils.printSuccess("Audit log saved to: " + filename);
        } catch (Exception e) {
            ConsoleUtils.printError("Error saving audit log: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    private void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            ConsoleUtils.printError("Username cannot be empty");
            return;
        }
        this.currentUser = username.trim();
        ConsoleUtils.printSuccess("Current user set to: " + this.currentUser);
        auditLog.log("SET_USER", "system", username, "User switched session");
    }

    private void clearConsole() {
        ConsoleUtils.clearConsole();
    }

    private void createDemoData() {
        // Создадим тестовые данные для демонстрации
        try {
            // Пользователи
            User admin = User.validate("admin", "System Administrator", "admin@company.com");
            User manager = User.validate("john_manager", "John Smith", "john@company.com");
            User analyst = User.validate("jane_analyst", "Jane Doe", "jane@company.com");
            
            userManager.add(admin);
            userManager.add(manager);
            userManager.add(analyst);

            // Роли
            Role adminRole = new Role("Administrator", "Full system access");
            Role managerRole = new Role("Manager", "Team management access");
            Role analystRole = new Role("Analyst", "Read-only access");

            adminRole.addPermission(new Permission("READ", "users", "Can view users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Can create/edit users"));
            adminRole.addPermission(new Permission("DELETE", "users", "Can delete users"));
            adminRole.addPermission(new Permission("READ", "reports", "Can view reports"));
            adminRole.addPermission(new Permission("WRITE", "reports", "Can create reports"));

            managerRole.addPermission(new Permission("READ", "users", "Can view users"));
            managerRole.addPermission(new Permission("WRITE", "users", "Can edit team members"));
            managerRole.addPermission(new Permission("READ", "reports", "Can view reports"));

            analystRole.addPermission(new Permission("READ", "users", "Can view users"));
            analystRole.addPermission(new Permission("READ", "reports", "Can view reports"));

            roleManager.add(adminRole);
            roleManager.add(managerRole);
            roleManager.add(analystRole);

            // Назначения
            AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Initial setup");
            AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Promotion");
            AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "New hire");

            PermanentAssignment assign1 = new PermanentAssignment(admin, adminRole, meta1);
            PermanentAssignment assign2 = new PermanentAssignment(manager, managerRole, meta2);
            
            String futureDate = DateUtils.addDays(DateUtils.getCurrentDateTime(), 30);
            TemporaryAssignment assign3 = new TemporaryAssignment(analyst, analystRole, meta3, futureDate);

            assignmentManager.add(assign1);
            assignmentManager.add(assign2);
            assignmentManager.add(assign3);

            // Записи аудита
            auditLog.log("CREATE_USER", "system", "admin", "Demo user created");
            auditLog.log("CREATE_USER", "system", "john_manager", "Demo user created");
            auditLog.log("CREATE_USER", "system", "jane_analyst", "Demo user created");
            auditLog.log("CREATE_ROLE", "system", "Administrator", "Demo role created");
            auditLog.log("CREATE_ROLE", "system", "Manager", "Demo role created");
            auditLog.log("CREATE_ROLE", "system", "Analyst", "Demo role created");
            auditLog.log("ASSIGN_ROLE", "system", "admin", "Role: Administrator, Type: PERMANENT");
            auditLog.log("ASSIGN_ROLE", "system", "john_manager", "Role: Manager, Type: PERMANENT");
            auditLog.log("ASSIGN_ROLE", "system", "jane_analyst", "Role: Analyst, Type: TEMPORARY");

        } catch (Exception e) {
            // Игнорируем ошибки при создании демо-данных
        }
    }

    public static void main(String[] args) {
        RBACConsoleApp app = new RBACConsoleApp();
        app.start();
    }
}
