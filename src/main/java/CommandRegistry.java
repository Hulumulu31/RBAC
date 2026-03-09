import java.util.*;
import java.util.stream.Collectors;

/**
 * Реестр команд системы RBAC.
 * Регистрирует все команды через лямбда-выражения.
 */
public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerServiceCommands(parser);
    }

    private static void registerUserCommands(CommandParser parser) {
        // user-list
        parser.registerCommand("user-list", "List all users with optional filters",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("All Users"));
                List<User> users = system.getUserManager().findAll();
                if (users.isEmpty()) {
                    System.out.println("No users found.");
                    return;
                }

                String[] headers = {"Username", "Full Name", "Email", "Active Roles"};
                List<String[]> rows = new ArrayList<>();

                for (User user : users) {
                    long activeRoles = system.getAssignmentManager().findByUser(user).stream()
                        .filter(RoleAssignment::isActive).count();
                    rows.add(new String[]{
                        user.username(),
                        user.fullName(),
                        user.email(),
                        String.valueOf(activeRoles)
                    });
                }

                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Total users: " + system.getUserManager().count());
            });

        // user-create
        parser.registerCommand("user-create", "Create a new user",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Create New User"));

                String username = ConsoleUtils.promptUsername(scanner, "Enter username: ", true);

                if (system.getUserManager().findByUsername(username).isPresent()) {
                    ConsoleUtils.printError("User with username '" + username + "' already exists");
                    return;
                }

                String fullName = ConsoleUtils.promptString(scanner, "Enter full name: ", true);
                String email = ConsoleUtils.promptEmail(scanner, "Enter email: ", true);

                try {
                    User user = User.validate(username, fullName, email);
                    system.getUserManager().add(user);
                    ConsoleUtils.printSuccess("User created successfully: " + user.format());
                } catch (IllegalArgumentException e) {
                    ConsoleUtils.printError("Error creating user: " + e.getMessage());
                }
            });

        // user-view
        parser.registerCommand("user-view", "View user details",
            (scanner, system) -> {
                String username = ConsoleUtils.promptString(scanner, "Enter username to view: ", true);
                User user = system.getUserManager().findByUsername(username).orElse(null);

                if (user == null) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                System.out.println(FormatUtils.formatBox(
                    "User: " + user.username() + "\n" +
                    "Full Name: " + user.fullName() + "\n" +
                    "Email: " + user.email()));

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive).collect(Collectors.toList());

                if (!activeAssignments.isEmpty()) {
                    System.out.println("\nActive Roles:");
                    for (RoleAssignment assignment : activeAssignments) {
                        System.out.println("  - " + assignment.role().getName() +
                            " (assigned: " + assignment.metadata().assignedAt() + ")");
                    }
                } else {
                    System.out.println("\nNo active roles assigned");
                }

                // Показываем все права
                Set<Permission> allPermissions = system.getAssignmentManager().getUserPermissions(user);
                if (!allPermissions.isEmpty()) {
                    System.out.println("\nAll Permissions:");
                    for (Permission perm : allPermissions) {
                        System.out.println("  - " + perm.format());
                    }
                }
            });

        // user-update
        parser.registerCommand("user-update", "Update user information",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Update User"));

                String username = ConsoleUtils.promptString(scanner, "Enter username to update: ", true);

                if (!system.getUserManager().exists(username)) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                User existingUser = system.getUserManager().findByUsername(username).orElse(null);
                System.out.println("Current info: " + existingUser.format());

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
                    system.getUserManager().update(username, newFullName, newEmail);
                    User updatedUser = system.getUserManager().findByUsername(username).orElse(null);
                    ConsoleUtils.printSuccess("User updated successfully: " + updatedUser.format());
                } catch (IllegalArgumentException e) {
                    ConsoleUtils.printError("Error updating user: " + e.getMessage());
                }
            });

        // user-delete
        parser.registerCommand("user-delete", "Delete a user",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Delete User"));

                String username = ConsoleUtils.promptString(scanner, "Enter username to delete: ", true);

                User userToRemove = system.getUserManager().findByUsername(username).orElse(null);
                if (userToRemove == null) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                List<RoleAssignment> userAssignments = system.getAssignmentManager().findByUser(userToRemove);
                long activeAssignments = userAssignments.stream().filter(RoleAssignment::isActive).count();

                if (activeAssignments > 0) {
                    ConsoleUtils.printWarning("This user has " + activeAssignments + " active role assignment(s).");
                }

                if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to delete user '" + username + "'")) {
                    for (RoleAssignment assignment : userAssignments) {
                        system.getAssignmentManager().remove(assignment);
                    }

                    system.getUserManager().remove(userToRemove);
                    ConsoleUtils.printSuccess("User deleted successfully");
                }
            });

        // user-search
        parser.registerCommand("user-search", "Search users by filters",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Search Users"));
                System.out.println("Search by:");
                System.out.println("  [1] Username (contains)");
                System.out.println("  [2] Email (contains)");
                System.out.println("  [3] Email domain");
                System.out.println("  [4] Full name (contains)");

                int choice = ConsoleUtils.promptInt(scanner, "Choose option (1-4): ", 1, 4);
                List<User> results = new ArrayList<>();

                switch (choice) {
                    case 1:
                        String usernamePattern = ConsoleUtils.promptString(scanner, "Enter username pattern: ", true);
                        results = system.getUserManager().findAll().stream()
                            .filter(u -> u.username().toLowerCase().contains(usernamePattern.toLowerCase()))
                            .collect(Collectors.toList());
                        break;
                    case 2:
                        String emailPattern = ConsoleUtils.promptString(scanner, "Enter email pattern: ", true);
                        results = system.getUserManager().findAll().stream()
                            .filter(u -> u.email().toLowerCase().contains(emailPattern.toLowerCase()))
                            .collect(Collectors.toList());
                        break;
                    case 3:
                        String domain = ConsoleUtils.promptString(scanner, "Enter email domain (e.g., gmail.com): ", true);
                        results = system.getUserManager().findAll().stream()
                            .filter(u -> u.email().toLowerCase().endsWith("@" + domain.toLowerCase()))
                            .collect(Collectors.toList());
                        break;
                    case 4:
                        String namePattern = ConsoleUtils.promptString(scanner, "Enter full name pattern: ", true);
                        results = system.getUserManager().findAll().stream()
                            .filter(u -> u.fullName().toLowerCase().contains(namePattern.toLowerCase()))
                            .collect(Collectors.toList());
                        break;
                }

                if (results.isEmpty()) {
                    System.out.println("No users found matching the criteria.");
                    return;
                }

                String[] headers = {"Username", "Full Name", "Email"};
                List<String[]> rows = new ArrayList<>();
                for (User user : results) {
                    rows.add(new String[]{user.username(), user.fullName(), user.email()});
                }
                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Found: " + results.size() + " user(s)");
            });
    }

    private static void registerRoleCommands(CommandParser parser) {
        // role-list
        parser.registerCommand("role-list", "List all roles",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("All Roles"));
                List<Role> roles = system.getRoleManager().findAll();
                if (roles.isEmpty()) {
                    System.out.println("No roles found.");
                    return;
                }

                String[] headers = {"Role Name", "ID", "Permissions", "Users"};
                List<String[]> rows = new ArrayList<>();

                for (Role role : roles) {
                    long userCount = system.getAssignmentManager().findByRole(role).stream()
                        .filter(RoleAssignment::isActive).count();
                    rows.add(new String[]{
                        role.getName(),
                        role.getId().substring(0, Math.min(8, role.getId().length())) + "...",
                        String.valueOf(role.getPermissions().size()),
                        String.valueOf(userCount)
                    });
                }

                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Total roles: " + system.getRoleManager().count());
            });

        // role-create
        parser.registerCommand("role-create", "Create a new role",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Create New Role"));

                String roleName = ConsoleUtils.promptString(scanner, "Enter role name: ", true);

                if (system.getRoleManager().findByName(roleName).isPresent()) {
                    ConsoleUtils.printError("Role with name '" + roleName + "' already exists");
                    return;
                }

                String description = ConsoleUtils.promptString(scanner, "Enter role description: ", true);

                try {
                    Role role = new Role(roleName, description);
                    system.getRoleManager().add(role);
                    ConsoleUtils.printSuccess("Role created successfully: " + role.getName());

                    // Предложить добавить права
                    if (ConsoleUtils.promptYesNo(scanner, "Add permissions to this role?")) {
                        boolean adding = true;
                        while (adding) {
                            String permName = ConsoleUtils.promptString(scanner, "Permission name (e.g., READ): ", true);
                            String resource = ConsoleUtils.promptString(scanner, "Resource (e.g., users): ", true);
                            String desc = ConsoleUtils.promptString(scanner, "Permission description: ", true);

                            Permission permission = new Permission(permName, resource, desc);
                            role.addPermission(permission);

                            ConsoleUtils.printSuccess("Permission added: " + permission.format());

                            adding = ConsoleUtils.promptYesNo(scanner, "Add another permission?");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    ConsoleUtils.printError("Error creating role: " + e.getMessage());
                }
            });

        // role-view
        parser.registerCommand("role-view", "View role details",
            (scanner, system) -> {
                String roleName = ConsoleUtils.promptString(scanner, "Enter role name to view: ", true);
                Role role = system.getRoleManager().findByName(roleName).orElse(null);

                if (role == null) {
                    ConsoleUtils.printError("Role not found: " + roleName);
                    return;
                }

                System.out.println(role.format());

                List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
                List<RoleAssignment> activeAssignments = assignments.stream()
                    .filter(RoleAssignment::isActive).collect(Collectors.toList());

                if (!activeAssignments.isEmpty()) {
                    System.out.println("Users with this role:");
                    for (RoleAssignment assignment : activeAssignments) {
                        System.out.println("  - " + assignment.user().username());
                    }
                } else {
                    System.out.println("No users have this role");
                }
            });

        // role-update
        parser.registerCommand("role-update", "Update role (name/description)",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Update Role"));

                String roleName = ConsoleUtils.promptString(scanner, "Enter role name to update: ", true);
                Role role = system.getRoleManager().findByName(roleName).orElse(null);

                if (role == null) {
                    ConsoleUtils.printError("Role not found: " + roleName);
                    return;
                }

                System.out.println("Current: " + role.getName() + " - " + role.getDescription());

                String newName = ConsoleUtils.promptString(scanner,
                    "Enter new name (or press Enter to keep current): ", false);
                String newDesc = ConsoleUtils.promptString(scanner,
                    "Enter new description (or press Enter to keep current): ", false);

                // Для обновления нужно создать новую роль (т.к. name final)
                if (!newName.isEmpty() && !newName.equals(role.getName())) {
                    if (system.getRoleManager().findByName(newName).isPresent()) {
                        ConsoleUtils.printError("Role with name '" + newName + "' already exists");
                        return;
                    }
                    // Удаляем старую и создаем новую
                    Role newRole = new Role(newName, newDesc.isEmpty() ? role.getDescription() : newDesc);
                    role.getPermissions().forEach(newRole::addPermission);
                    system.getRoleManager().remove(role);
                    system.getRoleManager().add(newRole);
                    ConsoleUtils.printSuccess("Role updated successfully");
                } else if (!newDesc.isEmpty()) {
                    // Обновляем только описание через создание новой роли
                    Role newRole = new Role(role.getName(), newDesc);
                    role.getPermissions().forEach(newRole::addPermission);
                    system.getRoleManager().remove(role);
                    system.getRoleManager().add(newRole);
                    ConsoleUtils.printSuccess("Role description updated successfully");
                } else {
                    ConsoleUtils.printInfo("No changes made");
                }
            });

        // role-delete
        parser.registerCommand("role-delete", "Delete a role",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Delete Role"));

                String roleName = ConsoleUtils.promptString(scanner, "Enter role name to delete: ", true);
                Role roleToRemove = system.getRoleManager().findByName(roleName).orElse(null);

                if (roleToRemove == null) {
                    ConsoleUtils.printError("Role not found: " + roleName);
                    return;
                }

                List<RoleAssignment> roleAssignments = system.getAssignmentManager().findByRole(roleToRemove);
                long activeAssignments = roleAssignments.stream().filter(RoleAssignment::isActive).count();

                if (activeAssignments > 0) {
                    ConsoleUtils.printWarning("This role is assigned to " + activeAssignments + " user(s):");
                    for (RoleAssignment assignment : roleAssignments) {
                        if (assignment.isActive()) {
                            System.out.println("  - " + assignment.user().username());
                        }
                    }
                }

                if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to delete role '" + roleName + "'")) {
                    for (RoleAssignment assignment : roleAssignments) {
                        system.getAssignmentManager().remove(assignment);
                    }
                    system.getRoleManager().remove(roleToRemove);
                    ConsoleUtils.printSuccess("Role deleted successfully");
                }
            });

        // role-add-permission
        parser.registerCommand("role-add-permission", "Add permission to a role",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Add Permission to Role"));

                String roleName = ConsoleUtils.promptString(scanner, "Enter role name: ", true);
                Role role = system.getRoleManager().findByName(roleName).orElse(null);

                if (role == null) {
                    ConsoleUtils.printError("Role not found: " + roleName);
                    return;
                }

                String permName = ConsoleUtils.promptString(scanner, "Permission name: ", true);
                String resource = ConsoleUtils.promptString(scanner, "Resource: ", true);
                String desc = ConsoleUtils.promptString(scanner, "Description: ", true);

                try {
                    Permission permission = new Permission(permName, resource, desc);
                    role.addPermission(permission);
                    ConsoleUtils.printSuccess("Permission added: " + permission.format());
                } catch (IllegalArgumentException e) {
                    ConsoleUtils.printError("Error: " + e.getMessage());
                }
            });

        // role-remove-permission
        parser.registerCommand("role-remove-permission", "Remove permission from a role",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Remove Permission from Role"));

                String roleName = ConsoleUtils.promptString(scanner, "Enter role name: ", true);
                Role role = system.getRoleManager().findByName(roleName).orElse(null);

                if (role == null) {
                    ConsoleUtils.printError("Role not found: " + roleName);
                    return;
                }

                Set<Permission> permissions = role.getPermissions();
                if (permissions.isEmpty()) {
                    System.out.println("This role has no permissions.");
                    return;
                }

                System.out.println("Permissions:");
                int i = 1;
                Map<Integer, Permission> permMap = new HashMap<>();
                for (Permission perm : permissions) {
                    System.out.println("  [" + i + "] " + perm.format());
                    permMap.put(i++, perm);
                }

                int choice = ConsoleUtils.promptInt(scanner, "Enter permission number to remove: ", 1, permissions.size());
                Permission toRemove = permMap.get(choice);
                role.removePermission(toRemove);
                ConsoleUtils.printSuccess("Permission removed");
            });

        // role-search
        parser.registerCommand("role-search", "Search roles by filters",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Search Roles"));
                System.out.println("Search by:");
                System.out.println("  [1] Name (contains)");
                System.out.println("  [2] Has specific permission");
                System.out.println("  [3] Minimum permissions count");

                int choice = ConsoleUtils.promptInt(scanner, "Choose option (1-3): ", 1, 3);
                List<Role> results = new ArrayList<>();

                switch (choice) {
                    case 1:
                        String namePattern = ConsoleUtils.promptString(scanner, "Enter role name pattern: ", true);
                        results = system.getRoleManager().findAll().stream()
                            .filter(r -> r.getName().toLowerCase().contains(namePattern.toLowerCase()))
                            .collect(Collectors.toList());
                        break;
                    case 2:
                        String permName = ConsoleUtils.promptString(scanner, "Enter permission name: ", true);
                        String resource = ConsoleUtils.promptString(scanner, "Enter resource: ", true);
                        results = system.getRoleManager().findAll().stream()
                            .filter(r -> r.hasPermission(permName, resource))
                            .collect(Collectors.toList());
                        break;
                    case 3:
                        int minPerms = ConsoleUtils.promptInt(scanner, "Enter minimum permissions count: ", 1, 100);
                        results = system.getRoleManager().findAll().stream()
                            .filter(r -> r.getPermissions().size() >= minPerms)
                            .collect(Collectors.toList());
                        break;
                }

                if (results.isEmpty()) {
                    System.out.println("No roles found matching the criteria.");
                    return;
                }

                String[] headers = {"Role Name", "Permissions", "Description"};
                List<String[]> rows = new ArrayList<>();
                for (Role role : results) {
                    rows.add(new String[]{
                        role.getName(),
                        String.valueOf(role.getPermissions().size()),
                        FormatUtils.truncate(role.getDescription(), 40)
                    });
                }
                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Found: " + results.size() + " role(s)");
            });
    }

    private static void registerAssignmentCommands(CommandParser parser) {
        // assign-role
        parser.registerCommand("assign-role", "Assign a role to a user",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Assign Role to User"));

                List<User> users = system.getUserManager().findAll();
                if (users.isEmpty()) {
                    ConsoleUtils.printError("No users available. Create a user first.");
                    return;
                }

                User user = ConsoleUtils.promptChoice(scanner, "Select user:", users,
                    u -> u.username() + " (" + u.fullName() + ")");

                List<Role> roles = system.getRoleManager().findAll();
                if (roles.isEmpty()) {
                    ConsoleUtils.printError("No roles available. Create a role first.");
                    return;
                }

                Role role = ConsoleUtils.promptChoice(scanner, "Select role:", roles, Role::getName);

                if (system.getAssignmentManager().findByUser(user).stream()
                    .anyMatch(a -> a.role().equals(role) && a.isActive())) {
                    ConsoleUtils.printError("User already has this role assigned");
                    return;
                }

                String assignedBy = ConsoleUtils.promptString(scanner, "Enter your username (assigner): ", true);
                String reason = ConsoleUtils.promptString(scanner, "Enter reason (optional): ", false);

                AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);

                System.out.println("\nAssignment type:");
                System.out.println("  [1] Permanent");
                System.out.println("  [2] Temporary");
                int typeChoice = ConsoleUtils.promptInt(scanner, "Choose type (1-2): ", 1, 2);

                try {
                    if (typeChoice == 1) {
                        var assignment = new PermanentAssignment(user, role, metadata);
                        system.getAssignmentManager().add(assignment);
                        ConsoleUtils.printSuccess("Permanent assignment created: " + assignment.summary());
                    } else {
                        String expiresAt = ConsoleUtils.promptDate(scanner,
                            "Enter expiration date (yyyy-MM-dd HH:mm:ss): ", true);
                        var assignment = new TemporaryAssignment(user, role, metadata, expiresAt);
                        system.getAssignmentManager().add(assignment);
                        ConsoleUtils.printSuccess("Temporary assignment created: " + assignment.summary());
                    }
                } catch (IllegalArgumentException e) {
                    ConsoleUtils.printError("Error creating assignment: " + e.getMessage());
                }
            });

        // revoke-role
        parser.registerCommand("revoke-role", "Revoke a role from a user",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Revoke Role"));

                String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);
                User user = system.getUserManager().findByUsername(username).orElse(null);

                if (user == null) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive).collect(Collectors.toList());

                if (assignments.isEmpty()) {
                    ConsoleUtils.printError("No active assignments for this user");
                    return;
                }

                System.out.println("Active assignments:");
                int i = 1;
                Map<Integer, RoleAssignment> assignmentMap = new HashMap<>();
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  [" + i + "] " + assignment.role().getName() +
                        " (" + assignment.assignmentType() + ")");
                    assignmentMap.put(i++, assignment);
                }

                int choice = ConsoleUtils.promptInt(scanner, "Enter assignment number to revoke: ", 1, assignments.size());
                RoleAssignment toRevoke = assignmentMap.get(choice);

                if (!(toRevoke instanceof PermanentAssignment)) {
                    ConsoleUtils.printError("Only permanent assignments can be revoked");
                    return;
                }

                if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to revoke this assignment")) {
                    ((PermanentAssignment) toRevoke).revoke();
                    ConsoleUtils.printSuccess("Assignment revoked");
                }
            });

        // assignment-list
        parser.registerCommand("assignment-list", "List all assignments",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("All Assignments"));
                List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
                if (assignments.isEmpty()) {
                    System.out.println("No assignments found.");
                    return;
                }

                String[] headers = {"Type", "User", "Role", "Status", "Assigned At"};
                List<String[]> rows = new ArrayList<>();

                for (RoleAssignment assignment : assignments) {
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
                System.out.println("Total assignments: " + system.getAssignmentManager().count());
            });

        // assignment-list-user
        parser.registerCommand("assignment-list-user", "List assignments for a specific user",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("User Assignments"));

                String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);
                User user = system.getUserManager().findByUsername(username).orElse(null);

                if (user == null) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                if (assignments.isEmpty()) {
                    System.out.println("No assignments for this user.");
                    return;
                }

                for (RoleAssignment assignment : assignments) {
                    System.out.println(assignment.summary());
                    System.out.println("---");
                }
            });

        // assignment-list-role
        parser.registerCommand("assignment-list-role", "List users with a specific role",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Role Assignments"));

                String roleName = ConsoleUtils.promptString(scanner, "Enter role name: ", true);
                Role role = system.getRoleManager().findByName(roleName).orElse(null);

                if (role == null) {
                    ConsoleUtils.printError("Role not found: " + roleName);
                    return;
                }

                List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role).stream()
                    .filter(RoleAssignment::isActive).collect(Collectors.toList());

                if (assignments.isEmpty()) {
                    System.out.println("No users have this role.");
                    return;
                }

                String[] headers = {"Username", "Full Name", "Assigned At", "Assigned By"};
                List<String[]> rows = new ArrayList<>();

                for (RoleAssignment assignment : assignments) {
                    rows.add(new String[]{
                        assignment.user().username(),
                        assignment.user().fullName(),
                        assignment.metadata().assignedAt(),
                        assignment.metadata().assignedBy()
                    });
                }

                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Total: " + assignments.size() + " user(s)");
            });

        // assignment-active
        parser.registerCommand("assignment-active", "List only active assignments",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Active Assignments"));
                List<RoleAssignment> assignments = system.getAssignmentManager().getActiveAssignments();
                if (assignments.isEmpty()) {
                    System.out.println("No active assignments found.");
                    return;
                }

                String[] headers = {"Type", "User", "Role", "Assigned At"};
                List<String[]> rows = new ArrayList<>();

                for (RoleAssignment assignment : assignments) {
                    rows.add(new String[]{
                        assignment.assignmentType(),
                        assignment.user().username(),
                        assignment.role().getName(),
                        assignment.metadata().assignedAt().substring(0, 10)
                    });
                }

                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Total active: " + assignments.size());
            });

        // assignment-expired
        parser.registerCommand("assignment-expired", "List expired temporary assignments",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Expired Assignments"));
                List<RoleAssignment> assignments = system.getAssignmentManager().getExpiredAssignments();
                if (assignments.isEmpty()) {
                    System.out.println("No expired assignments found.");
                    return;
                }

                String[] headers = {"Type", "User", "Role", "Status"};
                List<String[]> rows = new ArrayList<>();

                for (RoleAssignment assignment : assignments) {
                    rows.add(new String[]{
                        assignment.assignmentType(),
                        assignment.user().username(),
                        assignment.role().getName(),
                        "EXPIRED"
                    });
                }

                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Total expired: " + assignments.size());
            });

        // assignment-extend
        parser.registerCommand("assignment-extend", "Extend a temporary assignment",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Extend Temporary Assignment"));

                List<RoleAssignment> tempAssignments = system.getAssignmentManager().findAll().stream()
                    .filter(a -> a instanceof TemporaryAssignment && !a.isActive())
                    .collect(Collectors.toList());

                if (tempAssignments.isEmpty()) {
                    ConsoleUtils.printError("No expired temporary assignments found");
                    return;
                }

                System.out.println("Expired temporary assignments:");
                int i = 1;
                Map<Integer, RoleAssignment> assignmentMap = new HashMap<>();
                for (RoleAssignment assignment : tempAssignments) {
                    System.out.println("  [" + i + "] " + assignment.role().getName() +
                        " -> " + assignment.user().username());
                    assignmentMap.put(i++, assignment);
                }

                int choice = ConsoleUtils.promptInt(scanner, "Enter assignment number to extend: ", 1, tempAssignments.size());
                RoleAssignment toExtend = assignmentMap.get(choice);

                String newExpiration = ConsoleUtils.promptDate(scanner,
                    "Enter new expiration date (yyyy-MM-dd HH:mm:ss): ", true);

                ((TemporaryAssignment) toExtend).extend(newExpiration);
                ConsoleUtils.printSuccess("Assignment extended to: " + newExpiration);
            });

        // assignment-search
        // assignment-search
        parser.registerCommand("assignment-search", "Search assignments by filters",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Search Assignments"));
                System.out.println("Search by:");
                System.out.println("  [1] User");
                System.out.println("  [2] Role");
                System.out.println("  [3] Type (permanent/temporary)");
                System.out.println("  [4] Status (active/inactive)");
                System.out.println("  [5] Assigned after date");
                System.out.println("  [6] Expiring before date");

                int choice = ConsoleUtils.promptInt(scanner, "Choose option (1-6): ", 1, 6);
                List<RoleAssignment> results = new ArrayList<>();

                switch (choice) {
                    case 1:
                        String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);
                        User user = system.getUserManager().findByUsername(username).orElse(null);
                        if (user != null) {
                            results = system.getAssignmentManager().findByUser(user);
                        }
                        break;
                    case 2:
                        String roleName = ConsoleUtils.promptString(scanner, "Enter role name: ", true);
                        Role role = system.getRoleManager().findByName(roleName).orElse(null);
                        if (role != null) {
                            results = system.getAssignmentManager().findByRole(role);
                        }
                        break;
                    case 3:
                        String type = ConsoleUtils.promptString(scanner, "Enter type (PERMANENT/TEMPORARY): ", true);
                        results = system.getAssignmentManager().findAll().stream()
                            .filter(a -> a.assignmentType().equalsIgnoreCase(type))
                            .collect(Collectors.toList());
                        break;
                    case 4:
                        String status = ConsoleUtils.promptString(scanner, "Enter status (ACTIVE/INACTIVE): ", true);
                        results = system.getAssignmentManager().findAll().stream()
                            .filter(a -> {
                                boolean isActive = a.isActive();
                                return status.equalsIgnoreCase("ACTIVE") ? isActive : !isActive;
                            })
                            .collect(Collectors.toList());
                        break;
                    case 5:
                        String afterDate = ConsoleUtils.promptDate(scanner, "Enter date (yyyy-MM-dd HH:mm:ss): ", true);
                        results = system.getAssignmentManager().findAll().stream()
                            .filter(a -> a.metadata().assignedAt().compareTo(afterDate) > 0)
                            .collect(Collectors.toList());
                        break;
                    case 6:
                        String beforeDate = ConsoleUtils.promptDate(scanner, "Enter date (yyyy-MM-dd HH:mm:ss): ", true);
                        results = system.getAssignmentManager().findAll().stream()
                            .filter(a -> {
                                if (a instanceof TemporaryAssignment) {
                                    String expiresAt = ((TemporaryAssignment) a).getExpiresAt();
                                    return expiresAt.compareTo(beforeDate) < 0;
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
                        break;
                }

                if (results.isEmpty()) {
                    System.out.println("No assignments found matching the criteria.");
                    return;
                }

                String[] headers = {"Type", "User", "Role", "Status"};
                List<String[]> rows = new ArrayList<>();
                for (RoleAssignment assignment : results) {
                    rows.add(new String[]{
                        assignment.assignmentType(),
                        assignment.user().username(),
                        assignment.role().getName(),
                        assignment.isActive() ? "ACTIVE" : "INACTIVE"
                    });
                }
                System.out.println(FormatUtils.formatTable(headers, rows));
                System.out.println("Found: " + results.size() + " assignment(s)");
            });
    }

    private static void registerPermissionCommands(CommandParser parser) {
        // permissions-user
        parser.registerCommand("permissions-user", "Show all permissions for a user",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("User Permissions"));

                String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);
                User user = system.getUserManager().findByUsername(username).orElse(null);

                if (user == null) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                Set<Permission> allPermissions = system.getAssignmentManager().getUserPermissions(user);
                if (allPermissions.isEmpty()) {
                    System.out.println("User has no permissions.");
                    return;
                }

                // Группируем по ресурсам
                Map<String, List<Permission>> byResource = allPermissions.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

                for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                    System.out.println("\nResource: " + entry.getKey());
                    for (Permission perm : entry.getValue()) {
                        System.out.println("  - " + perm.name() + ": " + perm.description());
                    }
                }
            });

        // permissions-check
        parser.registerCommand("permissions-check", "Check if user has specific permission",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Check Permission"));

                String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);
                User user = system.getUserManager().findByUsername(username).orElse(null);

                if (user == null) {
                    ConsoleUtils.printError("User not found: " + username);
                    return;
                }

                String permName = ConsoleUtils.promptString(scanner, "Enter permission name: ", true);
                String resource = ConsoleUtils.promptString(scanner, "Enter resource: ", true);

                boolean hasPermission = system.getAssignmentManager().userHasPermission(user, permName, resource);

                if (hasPermission) {
                    ConsoleUtils.printSuccess("User HAS permission '" + permName + "' on '" + resource + "'");

                    // Находим, из какой роли это право
                    List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                    for (RoleAssignment assignment : assignments) {
                        if (assignment.isActive() && assignment.role().hasPermission(permName, resource)) {
                            System.out.println("  From role: " + assignment.role().getName());
                        }
                    }
                } else {
                    ConsoleUtils.printError("User does NOT have permission '" + permName + "' on '" + resource + "'");
                }
            });
    }

    private static void registerServiceCommands(CommandParser parser) {
        // help
        parser.registerCommand("help", "Show available commands",
            (scanner, system) -> parser.printHelp());

        // stats
        parser.registerCommand("stats", "Show system statistics",
            (scanner, system) -> {
                System.out.println(system.generateStatistics());
            });

        // clear
        parser.registerCommand("clear", "Clear console screen",
            (scanner, system) -> {
                ConsoleUtils.clearConsole();
            });

        // exit
        parser.registerCommand("exit", "Exit the application",
            (scanner, system) -> {
                if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to exit?")) {
                    System.out.println(FormatUtils.ANSI_CYAN + "Exiting RBAC Console Application... Goodbye!" + FormatUtils.ANSI_RESET);
                    System.exit(0);
                }
            });

        // save (опционально)
        parser.registerCommand("save", "Save data to file (simulation)",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Save Data"));
                ConsoleUtils.printInfo("Save functionality is simulated in this version.");
                ConsoleUtils.printSuccess("Data saved successfully (simulated)");
            });

        // load (опционально)
        parser.registerCommand("load", "Load data from file (simulation)",
            (scanner, system) -> {
                System.out.println(FormatUtils.formatSubHeader("Load Data"));
                ConsoleUtils.printInfo("Load functionality is simulated in this version.");
                ConsoleUtils.printSuccess("Data loaded successfully (simulated)");
            });
    }
}