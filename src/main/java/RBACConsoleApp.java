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
    private Scanner scanner;

    public RBACConsoleApp() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== RBAC Console Application ===");
        System.out.println("Welcome to the Role-Based Access Control system!");
        
        while (true) {
            printMainMenu();
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    manageUsers();
                    break;
                case 2:
                    manageRoles();
                    break;
                case 3:
                    manageAssignments();
                    break;
                case 4:
                    searchAndFilter();
                    break;
                case 5:
                    viewReports();
                    break;
                case 6:
                    saveAndLoadData();
                    break;
                case 0:
                    System.out.println("Exiting RBAC Console Application...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Manage Users");
        System.out.println("2. Manage Roles");
        System.out.println("3. Manage Assignments");
        System.out.println("4. Search and Filter");
        System.out.println("5. View Reports");
        System.out.println("6. Save/Load Data");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private void manageUsers() {
        System.out.println("\n--- User Management ---");
        System.out.println("1. Create User");
        System.out.println("2. Edit User");
        System.out.println("3. Delete User");
        System.out.println("4. View User");
        System.out.println("5. List All Users");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                createUser();
                break;
            case 2:
                editUser();
                break;
            case 3:
                deleteUser();
                break;
            case 4:
                viewUser();
                break;
            case 5:
                listAllUsers();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void createUser() {
        System.out.print("Enter username (3-20 chars, letters, digits, underscore only): ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        try {
            User user = User.validate(username, fullName, email);
            userManager.add(user);
            System.out.println("User created successfully: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    private void editUser() {
        System.out.print("Enter username to edit: ");
        String username = scanner.nextLine().trim();

        User existingUser = userManager.findByUsername(username).orElse(null);
        if (existingUser == null) {
            System.out.println("User not found: " + username);
            return;
        }

        System.out.println("Current user info: " + existingUser.format());
        System.out.print("Enter new full name (or press Enter to keep current): ");
        String newFullName = scanner.nextLine().trim();
        if (newFullName.isEmpty()) {
            newFullName = existingUser.fullName();
        }

        System.out.print("Enter new email (or press Enter to keep current): ");
        String newEmail = scanner.nextLine().trim();
        if (newEmail.isEmpty()) {
            newEmail = existingUser.email();
        }

        try {
            userManager.update(username, newFullName, newEmail);
            User updatedUser = userManager.findByUsername(username).orElse(null);
            System.out.println("User updated successfully: " + updatedUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        System.out.print("Enter username to delete: ");
        String username = scanner.nextLine().trim();

        User userToRemove = userManager.findByUsername(username).orElse(null);
        if (userToRemove == null) {
            System.out.println("User not found: " + username);
            return;
        }

        // Remove any assignments for this user first
        List<RoleAssignment> userAssignments = assignmentManager.findByUser(userToRemove);
        for (RoleAssignment assignment : userAssignments) {
            assignmentManager.remove(assignment);
        }

        userManager.remove(userToRemove);
        System.out.println("User deleted successfully: " + userToRemove.format());

        if (!userAssignments.isEmpty()) {
            System.out.println("Also removed " + userAssignments.size() + " assignment(s) for this user.");
        }
    }

    private void viewUser() {
        System.out.print("Enter username to view: ");
        String username = scanner.nextLine().trim();

        User user = userManager.findByUsername(username).orElse(null);
        if (user != null) {
            System.out.println("User details: " + user.format());
        } else {
            System.out.println("User not found: " + username);
        }
    }

    private void listAllUsers() {
        List<User> allUsers = userManager.findAll();
        if (allUsers.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("\n--- All Users ---");
        for (User user : allUsers) {
            System.out.println("- " + user.format());
        }
        System.out.println("Total users: " + userManager.count());
    }

    private void manageRoles() {
        System.out.println("\n--- Role Management ---");
        System.out.println("1. Create Role");
        System.out.println("2. Edit Role");
        System.out.println("3. Delete Role");
        System.out.println("4. View Role");
        System.out.println("5. List All Roles");
        System.out.println("6. Add Permission to Role");
        System.out.println("7. Remove Permission from Role");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                createRole();
                break;
            case 2:
                editRole();
                break;
            case 3:
                deleteRole();
                break;
            case 4:
                viewRole();
                break;
            case 5:
                listAllRoles();
                break;
            case 6:
                addPermissionToRole();
                break;
            case 7:
                removePermissionFromRole();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void createRole() {
        System.out.print("Enter role name: ");
        String roleName = scanner.nextLine().trim();

        System.out.print("Enter role description: ");
        String description = scanner.nextLine().trim();

        try {
            Role role = new Role(roleName, description);
            roleManager.add(role);
            System.out.println("Role created successfully: " + role.getName() + " (ID: " + role.getId() + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating role: " + e.getMessage());
        }
    }

    private void editRole() {
        System.out.print("Enter role ID to edit: ");
        String roleId = scanner.nextLine().trim();

        Role existingRole = roleManager.findById(roleId).orElse(null);
        if (existingRole == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        System.out.println("Current role: " + existingRole.getName() + " - " + existingRole.getDescription());
        System.out.print("Enter new role name (or press Enter to keep current): ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) {
            newName = existingRole.getName();
        }

        System.out.print("Enter new description (or press Enter to keep current): ");
        String newDescription = scanner.nextLine().trim();
        if (newDescription.isEmpty()) {
            newDescription = existingRole.getDescription();
        }

        try {
            // Создаем новую роль с обновленными данными
            Role updatedRole = new Role(newName, newDescription);
            // Копируем права из старой роли
            for (Permission perm : existingRole.getPermissions()) {
                updatedRole.addPermission(perm);
            }
            // Удаляем старую роль и добавляем новую
            roleManager.remove(existingRole);
            roleManager.add(updatedRole);
            System.out.println("Role updated successfully: " + updatedRole.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating role: " + e.getMessage());
        }
    }

    private void deleteRole() {
        System.out.print("Enter role ID to delete: ");
        String roleId = scanner.nextLine().trim();

        Role roleToRemove = roleManager.findById(roleId).orElse(null);
        if (roleToRemove == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        // Check if role is assigned to any users
        List<RoleAssignment> roleAssignments = assignmentManager.findByRole(roleToRemove);
        long activeAssignments = roleAssignments.stream().filter(RoleAssignment::isActive).count();
        if (activeAssignments > 0) {
            System.out.println("Warning: This role is assigned to " + activeAssignments + " user(s).");
            System.out.print("Are you sure you want to delete? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                return;
            }
        }

        // Remove all assignments for this role
        for (RoleAssignment assignment : roleAssignments) {
            assignmentManager.remove(assignment);
        }

        roleManager.remove(roleToRemove);
        System.out.println("Role deleted successfully: " + roleToRemove.getName());

        if (!roleAssignments.isEmpty()) {
            System.out.println("Also removed " + roleAssignments.size() + " assignment(s) for this role.");
        }
    }

    private void viewRole() {
        System.out.print("Enter role ID to view: ");
        String roleId = scanner.nextLine().trim();

        Role role = roleManager.findById(roleId).orElse(null);
        if (role != null) {
            System.out.println(role.format());
        } else {
            System.out.println("Role not found: " + roleId);
        }
    }

    private void listAllRoles() {
        List<Role> allRoles = roleManager.findAll();
        if (allRoles.isEmpty()) {
            System.out.println("No roles found.");
            return;
        }

        System.out.println("\n--- All Roles ---");
        for (Role role : allRoles) {
            System.out.println(role.format());
            System.out.println("---");
        }
        System.out.println("Total roles: " + roleManager.count());
    }

    private void addPermissionToRole() {
        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roleManager.findById(roleId).orElse(null);
        if (role == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        System.out.print("Enter permission name (e.g., READ): ");
        String permName = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter resource (e.g., users): ");
        String resource = scanner.nextLine().trim().toLowerCase();

        System.out.print("Enter permission description: ");
        String description = scanner.nextLine().trim();

        try {
            Permission permission = new Permission(permName, resource, description);
            role.addPermission(permission);
            System.out.println("Permission added to role: " + permission.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error adding permission: " + e.getMessage());
        }
    }

    private void removePermissionFromRole() {
        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roleManager.findById(roleId).orElse(null);
        if (role == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        System.out.println("Current permissions for role " + role.getName() + ":");
        for (Permission perm : role.getPermissions()) {
            System.out.println("- " + perm.format());
        }

        System.out.print("Enter permission name to remove: ");
        String permName = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter resource to remove: ");
        String resource = scanner.nextLine().trim().toLowerCase();

        Permission permissionToRemove = null;
        for (Permission perm : role.getPermissions()) {
            if (perm.name().equals(permName) && perm.resource().equals(resource)) {
                permissionToRemove = perm;
                break;
            }
        }

        if (permissionToRemove != null) {
            role.removePermission(permissionToRemove);
            System.out.println("Permission removed from role: " + permissionToRemove.format());
        } else {
            System.out.println("Permission not found for this role.");
        }
    }

    private void manageAssignments() {
        System.out.println("\n--- Assignment Management ---");
        System.out.println("1. Create Permanent Assignment");
        System.out.println("2. Create Temporary Assignment");
        System.out.println("3. Revoke Permanent Assignment");
        System.out.println("4. Extend Temporary Assignment");
        System.out.println("5. View Assignment");
        System.out.println("6. List All Assignments");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                createPermanentAssignment();
                break;
            case 2:
                createTemporaryAssignment();
                break;
            case 3:
                revokePermanentAssignment();
                break;
            case 4:
                extendTemporaryAssignment();
                break;
            case 5:
                viewAssignment();
                break;
            case 6:
                listAllAssignments();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void createPermanentAssignment() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        User user = userManager.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found: " + username);
            return;
        }

        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roleManager.findById(roleId).orElse(null);
        if (role == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        System.out.print("Enter assigned by (username): ");
        String assignedBy = scanner.nextLine().trim();

        System.out.print("Enter reason for assignment: ");
        String reason = scanner.nextLine().trim();

        AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);

        try {
            PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
            assignmentManager.add(assignment);
            System.out.println("Permanent assignment created: " + assignment.summary());
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating assignment: " + e.getMessage());
        }
    }

    private void createTemporaryAssignment() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        User user = userManager.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found: " + username);
            return;
        }

        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roleManager.findById(roleId).orElse(null);
        if (role == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        System.out.print("Enter assigned by (username): ");
        String assignedBy = scanner.nextLine().trim();

        System.out.print("Enter reason for assignment: ");
        String reason = scanner.nextLine().trim();

        System.out.print("Enter expiration date (yyyy-MM-dd HH:mm:ss): ");
        String expiresAt = scanner.nextLine().trim();

        AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);

        try {
            TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata, expiresAt);
            assignmentManager.add(assignment);
            System.out.println("Temporary assignment created: " + assignment.summary());
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating assignment: " + e.getMessage());
        }
    }

    private void revokePermanentAssignment() {
        System.out.print("Enter assignment ID to revoke: ");
        String assignmentId = scanner.nextLine().trim();

        try {
            assignmentManager.revokeAssignment(assignmentId);
            RoleAssignment assignment = assignmentManager.findById(assignmentId).orElse(null);
            if (assignment != null) {
                System.out.println("Permanent assignment revoked: " + assignment.summary());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error revoking assignment: " + e.getMessage());
        }
    }

    private void extendTemporaryAssignment() {
        System.out.print("Enter assignment ID to extend: ");
        String assignmentId = scanner.nextLine().trim();

        System.out.print("Enter new expiration date (yyyy-MM-dd HH:mm:ss): ");
        String newExpiresAt = scanner.nextLine().trim();

        try {
            assignmentManager.extendTemporaryAssignment(assignmentId, newExpiresAt);
            RoleAssignment assignment = assignmentManager.findById(assignmentId).orElse(null);
            if (assignment != null) {
                System.out.println("Temporary assignment extended: " + assignment.summary());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error extending assignment: " + e.getMessage());
        }
    }

    private void viewAssignment() {
        System.out.print("Enter assignment ID to view: ");
        String assignmentId = scanner.nextLine().trim();

        RoleAssignment assignment = assignmentManager.findById(assignmentId).orElse(null);
        if (assignment != null) {
            System.out.println(assignment.summary());
        } else {
            System.out.println("Assignment not found: " + assignmentId);
        }
    }

    private void listAllAssignments() {
        List<RoleAssignment> allAssignments = assignmentManager.findAll();
        if (allAssignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        System.out.println("\n--- All Assignments ---");
        for (RoleAssignment assignment : allAssignments) {
            System.out.println(assignment.summary());
            System.out.println("---");
        }
        System.out.println("Total assignments: " + assignmentManager.count());
    }

    private void searchAndFilter() {
        System.out.println("\n--- Search and Filter ---");
        System.out.println("1. Search Users");
        System.out.println("2. Search Roles");
        System.out.println("3. Search Assignments");
        System.out.println("4. Filter Users (Advanced)");
        System.out.println("5. Filter Roles (Advanced)");
        System.out.println("6. Filter Assignments (Advanced)");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                searchUsers();
                break;
            case 2:
                searchRoles();
                break;
            case 3:
                searchAssignments();
                break;
            case 4:
                filterUsersAdvanced();
                break;
            case 5:
                filterRolesAdvanced();
                break;
            case 6:
                filterAssignmentsAdvanced();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void searchUsers() {
        System.out.print("Enter search term for users: ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        UserFilter filter = UserFilters.byUsernameContains(searchTerm)
                .or(UserFilters.byFullNameContains(searchTerm))
                .or(UserFilters.byEmailDomain(searchTerm));

        List<User> matchingUsers = userManager.findByFilter(filter);

        if (matchingUsers.isEmpty()) {
            System.out.println("No users found matching: " + searchTerm);
        } else {
            System.out.println("\n--- Matching Users ---");
            for (User user : matchingUsers) {
                System.out.println("- " + user.format());
            }
            System.out.println("Found " + matchingUsers.size() + " user(s).");
        }
    }

    private void searchRoles() {
        System.out.print("Enter search term for roles: ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        RoleFilter filter = RoleFilters.byNameContains(searchTerm);

        List<Role> matchingRoles = roleManager.findByFilter(filter);

        if (matchingRoles.isEmpty()) {
            System.out.println("No roles found matching: " + searchTerm);
        } else {
            System.out.println("\n--- Matching Roles ---");
            for (Role role : matchingRoles) {
                System.out.println(role.format());
                System.out.println("---");
            }
            System.out.println("Found " + matchingRoles.size() + " role(s).");
        }
    }

    private void searchAssignments() {
        System.out.print("Enter search term for assignments (username, rolename, assignedby): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        AssignmentFilter filter = AssignmentFilters.byUsername(searchTerm)
                .or(AssignmentFilters.byRoleName(searchTerm))
                .or(AssignmentFilters.assignedBy(searchTerm));

        List<RoleAssignment> matchingAssignments = assignmentManager.findByFilter(filter);

        if (matchingAssignments.isEmpty()) {
            System.out.println("No assignments found matching: " + searchTerm);
        } else {
            System.out.println("\n--- Matching Assignments ---");
            for (RoleAssignment assignment : matchingAssignments) {
                System.out.println(assignment.summary());
                System.out.println("---");
            }
            System.out.println("Found " + matchingAssignments.size() + " assignment(s).");
        }
    }

    private void filterUsersAdvanced() {
        System.out.println("\n--- Advanced User Filter ---");
        System.out.println("1. Filter by username (exact)");
        System.out.println("2. Filter by username (contains)");
        System.out.println("3. Filter by email domain");
        System.out.println("4. Filter by full name (contains)");
        System.out.println("5. Combine filters (AND)");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                System.out.print("Enter username: ");
                String username = scanner.nextLine().trim();
                showFilteredUsers(UserFilters.byUsername(username));
                break;
            case 2:
                System.out.print("Enter username substring: ");
                String substr = scanner.nextLine().trim();
                showFilteredUsers(UserFilters.byUsernameContains(substr));
                break;
            case 3:
                System.out.print("Enter email domain (e.g., @company.com): ");
                String domain = scanner.nextLine().trim();
                showFilteredUsers(UserFilters.byEmailDomain(domain));
                break;
            case 4:
                System.out.print("Enter full name substring: ");
                String nameSubstr = scanner.nextLine().trim();
                showFilteredUsers(UserFilters.byFullNameContains(nameSubstr));
                break;
            case 5:
                System.out.print("Enter username substring: ");
                String userSubstr = scanner.nextLine().trim();
                System.out.print("Enter email domain: ");
                String emailDomain = scanner.nextLine().trim();
                UserFilter combined = UserFilters.byUsernameContains(userSubstr)
                        .and(UserFilters.byEmailDomain(emailDomain));
                showFilteredUsers(combined);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void showFilteredUsers(UserFilter filter) {
        System.out.println("\n1. Sort by username");
        System.out.println("2. Sort by full name");
        System.out.println("3. Sort by email");
        System.out.println("4. No sorting");
        System.out.print("Choose sorting option: ");

        int sortChoice = getIntInput();
        Comparator<User> sorter = null;

        switch (sortChoice) {
            case 1:
                sorter = UserSorters.byUsername();
                break;
            case 2:
                sorter = UserSorters.byFullName();
                break;
            case 3:
                sorter = UserSorters.byEmail();
                break;
        }

        List<User> results = userManager.findAll(filter, sorter);

        if (results.isEmpty()) {
            System.out.println("No users found matching the filter.");
        } else {
            System.out.println("\n--- Filtered Users ---");
            for (User user : results) {
                System.out.println("- " + user.format());
            }
            System.out.println("Found " + results.size() + " user(s).");
        }
    }

    private void filterRolesAdvanced() {
        System.out.println("\n--- Advanced Role Filter ---");
        System.out.println("1. Filter by role name (exact)");
        System.out.println("2. Filter by role name (contains)");
        System.out.println("3. Filter by minimum permissions count");
        System.out.println("4. Filter by permission (name and resource)");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                System.out.print("Enter role name: ");
                String roleName = scanner.nextLine().trim();
                showFilteredRoles(RoleFilters.byName(roleName));
                break;
            case 2:
                System.out.print("Enter role name substring: ");
                String substr = scanner.nextLine().trim();
                showFilteredRoles(RoleFilters.byNameContains(substr));
                break;
            case 3:
                System.out.print("Enter minimum permissions count: ");
                int minPerms = getIntInput();
                showFilteredRoles(RoleFilters.hasAtLeastNPermissions(minPerms));
                break;
            case 4:
                System.out.print("Enter permission name: ");
                String permName = scanner.nextLine().trim();
                System.out.print("Enter resource: ");
                String resource = scanner.nextLine().trim();
                showFilteredRoles(RoleFilters.hasPermission(permName, resource));
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void showFilteredRoles(RoleFilter filter) {
        System.out.println("\n1. Sort by name");
        System.out.println("2. Sort by permission count");
        System.out.println("3. No sorting");
        System.out.print("Choose sorting option: ");

        int sortChoice = getIntInput();
        Comparator<Role> sorter = null;

        switch (sortChoice) {
            case 1:
                sorter = RoleSorters.byName();
                break;
            case 2:
                sorter = RoleSorters.byPermissionCount();
                break;
        }

        List<Role> results = roleManager.findAll(filter, sorter);

        if (results.isEmpty()) {
            System.out.println("No roles found matching the filter.");
        } else {
            System.out.println("\n--- Filtered Roles ---");
            for (Role role : results) {
                System.out.println(role.format());
                System.out.println("---");
            }
            System.out.println("Found " + results.size() + " role(s).");
        }
    }

    private void filterAssignmentsAdvanced() {
        System.out.println("\n--- Advanced Assignment Filter ---");
        System.out.println("1. Filter by username");
        System.out.println("2. Filter by role name");
        System.out.println("3. Filter by type (PERMANENT/TEMPORARY)");
        System.out.println("4. Filter active only");
        System.out.println("5. Filter inactive only");
        System.out.println("6. Filter by who assigned");
        System.out.println("7. Combined: Active + Type");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                System.out.print("Enter username: ");
                String username = scanner.nextLine().trim();
                showFilteredAssignments(AssignmentFilters.byUsername(username));
                break;
            case 2:
                System.out.print("Enter role name: ");
                String roleName = scanner.nextLine().trim();
                showFilteredAssignments(AssignmentFilters.byRoleName(roleName));
                break;
            case 3:
                System.out.print("Enter type (PERMANENT/TEMPORARY): ");
                String type = scanner.nextLine().trim();
                showFilteredAssignments(AssignmentFilters.byType(type));
                break;
            case 4:
                showFilteredAssignments(AssignmentFilters.activeOnly());
                break;
            case 5:
                showFilteredAssignments(AssignmentFilters.inactiveOnly());
                break;
            case 6:
                System.out.print("Enter assigned by username: ");
                String assignedBy = scanner.nextLine().trim();
                showFilteredAssignments(AssignmentFilters.assignedBy(assignedBy));
                break;
            case 7:
                System.out.print("Enter type (PERMANENT/TEMPORARY): ");
                String combinedType = scanner.nextLine().trim();
                AssignmentFilter combined = AssignmentFilters.activeOnly()
                        .and(AssignmentFilters.byType(combinedType));
                showFilteredAssignments(combined);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void showFilteredAssignments(AssignmentFilter filter) {
        System.out.println("\n1. Sort by username");
        System.out.println("2. Sort by role name");
        System.out.println("3. Sort by assignment date");
        System.out.println("4. No sorting");
        System.out.print("Choose sorting option: ");

        int sortChoice = getIntInput();
        Comparator<RoleAssignment> sorter = null;

        switch (sortChoice) {
            case 1:
                sorter = AssignmentSorters.byUsername();
                break;
            case 2:
                sorter = AssignmentSorters.byRoleName();
                break;
            case 3:
                sorter = AssignmentSorters.byAssignmentDate();
                break;
        }

        List<RoleAssignment> results = assignmentManager.findAll(filter, sorter);

        if (results.isEmpty()) {
            System.out.println("No assignments found matching the filter.");
        } else {
            System.out.println("\n--- Filtered Assignments ---");
            for (RoleAssignment assignment : results) {
                System.out.println(assignment.summary());
                System.out.println("---");
            }
            System.out.println("Found " + results.size() + " assignment(s).");
        }
    }

    private void viewReports() {
        System.out.println("\n--- Reports ---");
        System.out.println("1. User Statistics");
        System.out.println("2. Role Statistics");
        System.out.println("3. Assignment Statistics");
        System.out.println("4. Permissions by Role");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                userStatistics();
                break;
            case 2:
                roleStatistics();
                break;
            case 3:
                assignmentStatistics();
                break;
            case 4:
                permissionsByRole();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void userStatistics() {
        System.out.println("\n--- User Statistics ---");
        System.out.println("Total users: " + userManager.count());

        List<User> allUsers = userManager.findAll();
        if (!allUsers.isEmpty()) {
            // Find most common domain in emails
            Map<String, Long> domainCount = allUsers.stream()
                    .map(user -> user.email().substring(user.email().lastIndexOf('@') + 1))
                    .collect(Collectors.groupingBy(domain -> domain, Collectors.counting()));

            String topDomain = domainCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            System.out.println("Most common email domain: " + topDomain);
        }
    }

    private void roleStatistics() {
        System.out.println("\n--- Role Statistics ---");
        System.out.println("Total roles: " + roleManager.count());

        List<Role> allRoles = roleManager.findAll();
        if (!allRoles.isEmpty()) {
            // Find role with most permissions
            Role largestRole = allRoles.stream()
                    .max(Comparator.comparingInt(role -> role.getPermissions().size()))
                    .orElse(null);

            if (largestRole != null) {
                System.out.println("Role with most permissions: " + largestRole.getName() +
                                 " (" + largestRole.getPermissions().size() + " permissions)");
            }
        }
    }

    private void assignmentStatistics() {
        System.out.println("\n--- Assignment Statistics ---");
        System.out.println("Total assignments: " + assignmentManager.count());

        long permanentCount = assignmentManager.findAll().stream()
                .filter(assignment -> assignment instanceof PermanentAssignment)
                .count();

        long temporaryCount = assignmentManager.findAll().stream()
                .filter(assignment -> assignment instanceof TemporaryAssignment)
                .count();

        System.out.println("Permanent assignments: " + permanentCount);
        System.out.println("Temporary assignments: " + temporaryCount);

        long activeCount = assignmentManager.getActiveAssignments().stream().count();

        System.out.println("Active assignments: " + activeCount);
        System.out.println("Inactive assignments: " + (assignmentManager.count() - activeCount));
    }

    private void permissionsByRole() {
        System.out.println("\n--- Permissions by Role ---");
        List<Role> allRoles = roleManager.findAll();
        if (allRoles.isEmpty()) {
            System.out.println("No roles found.");
            return;
        }

        for (Role role : allRoles) {
            System.out.println("Role: " + role.getName());
            if (role.getPermissions().isEmpty()) {
                System.out.println("  No permissions assigned");
            } else {
                for (Permission perm : role.getPermissions()) {
                    System.out.println("  - " + perm.format());
                }
            }
            System.out.println();
        }
    }

    private void saveAndLoadData() {
        System.out.println("\n--- Save/Load Data ---");
        System.out.println("Note: This is a simplified implementation.");
        System.out.println("In a real application, you would implement serialization to/from files.");
        System.out.println("1. Save Data (simulated)");
        System.out.println("2. Load Data (simulated)");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                System.out.println("Data saved successfully to file (simulation).");
                System.out.println("Users: " + userManager.count() +
                                 ", Roles: " + roleManager.count() +
                                 ", Assignments: " + assignmentManager.count());
                break;
            case 2:
                System.out.println("Data loaded successfully from file (simulation).");
                System.out.println("Current counts - Users: " + userManager.count() +
                                 ", Roles: " + roleManager.count() +
                                 ", Assignments: " + assignmentManager.count());
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private int getIntInput() {
        while (true) {
            try {
                int result = Integer.parseInt(scanner.nextLine().trim());
                return result;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    public static void main(String[] args) {
        // Run comprehensive tests for all components
        runAllTests();
        
        // Then start the console application
        RBACConsoleApp app = new RBACConsoleApp();
        app.start();
    }
    
    public static void runAllTests() {
        System.out.println("=== Running Comprehensive Tests ===\n");

        // Test User
        testUser();
        System.out.println();

        // Test Permission
        testPermission();
        System.out.println();

        // Test Role
        testRole();
        System.out.println();

        // Test AssignmentMetadata
        testAssignmentMetadata();
        System.out.println();

        // Test PermanentAssignment
        testPermanentAssignment();
        System.out.println();

        // Test TemporaryAssignment
        testTemporaryAssignment();
        System.out.println();

        // Test Filters
        testUserFilters();
        System.out.println();

        testRoleFilters();
        System.out.println();

        testAssignmentFilters();
        System.out.println();

        // Test Sorters
        testUserSorters();
        System.out.println();

        testRoleSorters();
        System.out.println();

        testAssignmentSorters();
        System.out.println();

        // Test Managers
        testUserManager();
        System.out.println();

        testRoleManager();
        System.out.println();

        testAssignmentManager();
        System.out.println();

        System.out.println("=== All Tests Completed ===\n");
    }
    
    public static void testUser() {
        System.out.println("--- Testing User ---");
        // Тестирование валидации
        try {
            // Корректный пользователь
            User user1 = User.validate("john_doe", "John Doe", "john@example.com");
            System.out.println("Created user: " + user1.format());

            // Некорректные пользователи для тестирования
            try {
                User.validate("", "Jane Doe", "jane@example.com");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for empty username: " + e.getMessage());
            }

            try {
                User.validate("ab", "Jane Doe", "jane@example.com");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for short username: " + e.getMessage());
            }

            try {
                User.validate("user@name", "Jane Doe", "jane@example.com");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for invalid username: " + e.getMessage());
            }

            try {
                User.validate("jane_doe", "Jane Doe", "invalid-email");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for invalid email: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void testPermission() {
        System.out.println("--- Testing Permission ---");
        try {
            // Тестирование корректного Permission
            Permission perm1 = new Permission("read", "USERS", "Can view user list");
            System.out.println("Created permission: " + perm1.format());

            // Тестирование поиска по шаблонам
            System.out.println("Matches READ/users: " + perm1.matches("READ", "users"));
            System.out.println("Matches READ/reports: " + perm1.matches("READ", "reports"));

            // Тестирование некорректных значений
            try {
                new Permission("", "users", "description");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for empty name: " + e.getMessage());
            }

            try {
                new Permission("READ WITH SPACE", "users", "description");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for name with space: " + e.getMessage());
            }

            try {
                new Permission("READ", "", "description");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for empty resource: " + e.getMessage());
            }

            try {
                new Permission("READ", "USERS", "");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error for empty description: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void testRole() {
        System.out.println("--- Testing Role ---");
        try {
            // Тестирование создания роли
            Role adminRole = new Role("Administrator", "Full system access");
            System.out.println("Created role: " + adminRole.getName());

            // Тестирование добавления прав
            Permission readPerm = new Permission("READ", "users", "Can view user list");
            Permission writePerm = new Permission("WRITE", "users", "Can create and edit users");

            adminRole.addPermission(readPerm);
            adminRole.addPermission(writePerm);

            System.out.println("Added permissions to role");
            System.out.println(adminRole.format());

            // Тестирование проверки прав
            System.out.println("Has READ permission on users: " +
                             adminRole.hasPermission("READ", "users"));
            System.out.println("Has DELETE permission on users: " +
                             adminRole.hasPermission("DELETE", "users"));

            // Тестирование удаления прав
            adminRole.removePermission(readPerm);
            System.out.println("After removing READ permission:");
            System.out.println("Has READ permission on users: " +
                             adminRole.hasPermission("READ", "users"));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAssignmentMetadata() {
        System.out.println("--- Testing AssignmentMetadata ---");
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

    public static void testPermanentAssignment() {
        System.out.println("--- Testing PermanentAssignment ---");
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

    public static void testTemporaryAssignment() {
        System.out.println("--- Testing TemporaryAssignment ---");
        try {
            // Создание тестовых объектов
            User user = User.validate("jane_doe", "Jane Doe", "jane@example.com");
            Role viewerRole = new Role("Viewer", "Limited read access");
            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Temporary access for project");

            // Создание временного назначения (истекает через 1 минуту)
            String futureDate = java.time.LocalDateTime.now().plusMinutes(1).format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            TemporaryAssignment tempAssign = new TemporaryAssignment(user, viewerRole, metadata, futureDate);

            System.out.println("Temporary assignment created:");
            System.out.println(tempAssign.summary());
            System.out.println("Is active: " + tempAssign.isActive());
            System.out.println("Is expired: " + tempAssign.isExpired());

            // Продление назначения
            String extendedDate = java.time.LocalDateTime.now().plusHours(1).format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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

    public static void testUserFilters() {
        System.out.println("--- Testing UserFilters ---");
        try {
            User user1 = User.validate("john_doe", "John Doe", "john@company.com");
            User user2 = User.validate("jane_smith", "Jane Smith", "jane@example.com");

            UserFilter byUsername = UserFilters.byUsername("john_doe");
            System.out.println("byUsername(john_doe) test on john_doe: " + byUsername.test(user1));
            System.out.println("byUsername(john_doe) test on jane_smith: " + byUsername.test(user2));

            UserFilter byDomain = UserFilters.byEmailDomain("@company.com");
            System.out.println("byEmailDomain(@company.com) test on john: " + byDomain.test(user1));
            System.out.println("byEmailDomain(@company.com) test on jane: " + byDomain.test(user2));

            UserFilter combined = byUsername.and(byDomain);
            System.out.println("Combined (AND) filter test on john: " + combined.test(user1));

            UserFilter orFilter = UserFilters.byUsername("john_doe").or(UserFilters.byUsername("jane_smith"));
            System.out.println("Combined (OR) filter: " + orFilter.test(user1));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testRoleFilters() {
        System.out.println("--- Testing RoleFilters ---");
        try {
            Role adminRole = new Role("Administrator", "Full access");
            adminRole.addPermission(new Permission("READ", "users", "Can read users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Can write users"));

            Role viewerRole = new Role("Viewer", "Read only");
            viewerRole.addPermission(new Permission("READ", "users", "Can read users"));

            RoleFilter byName = RoleFilters.byName("Administrator");
            System.out.println("byName(Administrator): " + byName.test(adminRole));

            RoleFilter hasPerm = RoleFilters.hasPermission("WRITE", "users");
            System.out.println("hasPermission(WRITE, users) on admin: " + hasPerm.test(adminRole));
            System.out.println("hasPermission(WRITE, users) on viewer: " + hasPerm.test(viewerRole));

            RoleFilter minPerms = RoleFilters.hasAtLeastNPermissions(2);
            System.out.println("hasAtLeastNPermissions(2) on admin: " + minPerms.test(adminRole));
            System.out.println("hasAtLeastNPermissions(2) on viewer: " + minPerms.test(viewerRole));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAssignmentFilters() {
        System.out.println("--- Testing AssignmentFilters ---");
        try {
            User user = User.validate("test_user", "Test User", "test@example.com");
            Role role = new Role("TestRole", "Test role");
            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test");

            PermanentAssignment permAssign = new PermanentAssignment(user, role, metadata);

            AssignmentFilter byUsername = AssignmentFilters.byUsername("test_user");
            System.out.println("byUsername(test_user): " + byUsername.test(permAssign));

            AssignmentFilter activeOnly = AssignmentFilters.activeOnly();
            System.out.println("activeOnly on permanent: " + activeOnly.test(permAssign));

            AssignmentFilter byType = AssignmentFilters.byType("PERMANENT");
            System.out.println("byType(PERMANENT): " + byType.test(permAssign));

            AssignmentFilter combined = activeOnly.and(byType);
            System.out.println("Combined (active AND PERMANENT): " + combined.test(permAssign));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testUserSorters() {
        System.out.println("--- Testing UserSorters ---");
        try {
            User user1 = User.validate("charlie", "Charlie Brown", "charlie@example.com");
            User user2 = User.validate("alice", "Alice Wonder", "alice@example.com");
            User user3 = User.validate("bob", "Bob Builder", "bob@example.com");

            List<User> users = Arrays.asList(user1, user2, user3);

            users.sort(UserSorters.byUsername());
            System.out.println("Sorted by username: " + users.stream().map(User::username).collect(Collectors.joining(", ")));

            users.sort(UserSorters.byFullName());
            System.out.println("Sorted by fullName: " + users.stream().map(User::fullName).collect(Collectors.joining(", ")));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testRoleSorters() {
        System.out.println("--- Testing RoleSorters ---");
        try {
            Role role1 = new Role("Admin", "Administrator");
            role1.addPermission(new Permission("READ", "users", "Read"));
            role1.addPermission(new Permission("WRITE", "users", "Write"));
            role1.addPermission(new Permission("DELETE", "users", "Delete"));

            Role role2 = new Role("Viewer", "Viewer");
            role2.addPermission(new Permission("READ", "users", "Read"));

            Role role3 = new Role("Manager", "Manager");
            role3.addPermission(new Permission("READ", "users", "Read"));
            role3.addPermission(new Permission("WRITE", "users", "Write"));

            List<Role> roles = Arrays.asList(role1, role2, role3);

            roles.sort(RoleSorters.byName());
            System.out.println("Sorted by name: " + roles.stream().map(Role::getName).collect(Collectors.joining(", ")));

            roles.sort(RoleSorters.byPermissionCount());
            System.out.println("Sorted by permission count: " +
                roles.stream().map(r -> r.getName() + "(" + r.getPermissions().size() + ")").collect(Collectors.joining(", ")));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAssignmentSorters() {
        System.out.println("--- Testing AssignmentSorters ---");
        try {
            User user1 = User.validate("alice", "Alice", "alice@example.com");
            User user2 = User.validate("bob", "Bob", "bob@example.com");
            Role role1 = new Role("Admin", "Admin");
            Role role2 = new Role("Viewer", "Viewer");

            AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test 1");
            AssignmentMetadata metadata2 = AssignmentMetadata.now("admin", "Test 2");

            PermanentAssignment assign1 = new PermanentAssignment(user1, role2, metadata1);
            PermanentAssignment assign2 = new PermanentAssignment(user2, role1, metadata2);

            List<RoleAssignment> assignments = Arrays.asList(assign1, assign2);

            assignments.sort(AssignmentSorters.byUsername());
            System.out.println("Sorted by username: " +
                assignments.stream().map(a -> a.user().username()).collect(Collectors.joining(", ")));

            assignments.sort(AssignmentSorters.byRoleName());
            System.out.println("Sorted by role name: " +
                assignments.stream().map(a -> a.role().getName()).collect(Collectors.joining(", ")));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testUserManager() {
        System.out.println("--- Testing UserManager ---");
        try {
            UserManager userManager = new UserManager();

            // Add users
            User user1 = User.validate("john_doe", "John Doe", "john@company.com");
            User user2 = User.validate("jane_smith", "Jane Smith", "jane@example.com");

            userManager.add(user1);
            userManager.add(user2);

            System.out.println("Users count: " + userManager.count());

            // Find by username
            Optional<User> found = userManager.findByUsername("john_doe");
            System.out.println("Found john_doe: " + found.isPresent());

            // Find by email
            Optional<User> byEmail = userManager.findByEmail("jane@example.com");
            System.out.println("Found by email: " + byEmail.isPresent());

            // Filter users
            List<User> filtered = userManager.findByFilter(UserFilters.byEmailDomain("@company.com"));
            System.out.println("Users with @company.com: " + filtered.size());

            // Update user
            userManager.update("john_doe", "John Updated", "john.updated@company.com");
            User updated = userManager.findByUsername("john_doe").orElse(null);
            System.out.println("Updated user email: " + (updated != null ? updated.email() : "N/A"));

            // Exists check
            System.out.println("john_doe exists: " + userManager.exists("john_doe"));

            // Remove user
            userManager.remove(user2);
            System.out.println("After removal, count: " + userManager.count());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testRoleManager() {
        System.out.println("--- Testing RoleManager ---");
        try {
            RoleManager roleManager = new RoleManager();

            // Add roles
            Role adminRole = new Role("Administrator", "Full access");
            adminRole.addPermission(new Permission("READ", "users", "Read users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Write users"));

            Role viewerRole = new Role("Viewer", "Read only");
            viewerRole.addPermission(new Permission("READ", "users", "Read users"));

            roleManager.add(adminRole);
            roleManager.add(viewerRole);

            System.out.println("Roles count: " + roleManager.count());

            // Find by name
            Optional<Role> found = roleManager.findByName("Administrator");
            System.out.println("Found Administrator: " + found.isPresent());

            // Find by filter
            List<Role> withWritePerm = roleManager.findRolesWithPermission("WRITE", "users");
            System.out.println("Roles with WRITE permission: " + withWritePerm.size());

            // Add permission
            roleManager.addPermissionToRole("Viewer", new Permission("READ", "reports", "Read reports"));
            Role viewer = roleManager.findByName("Viewer").orElse(null);
            System.out.println("Viewer permissions count: " + (viewer != null ? viewer.getPermissions().size() : 0));

            // Remove permission
            roleManager.removePermissionFromRole("Viewer", new Permission("READ", "reports", "Read reports"));
            viewer = roleManager.findByName("Viewer").orElse(null);
            System.out.println("After removal, Viewer permissions: " + (viewer != null ? viewer.getPermissions().size() : 0));

            // Exists check
            System.out.println("Administrator exists: " + roleManager.exists("Administrator"));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAssignmentManager() {
        System.out.println("--- Testing AssignmentManager ---");
        try {
            UserManager userManager = new UserManager();
            RoleManager roleManager = new RoleManager();
            AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);

            // Setup users and roles
            User user = User.validate("test_user", "Test User", "test@example.com");
            Role adminRole = new Role("Administrator", "Full access");
            adminRole.addPermission(new Permission("READ", "users", "Read users"));
            adminRole.addPermission(new Permission("WRITE", "users", "Write users"));

            Role viewerRole = new Role("Viewer", "Read only");
            viewerRole.addPermission(new Permission("READ", "users", "Read users"));

            userManager.add(user);
            roleManager.add(adminRole);
            roleManager.add(viewerRole);

            // Create assignments
            AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test assignment 1");
            AssignmentMetadata metadata2 = AssignmentMetadata.now("admin", "Test assignment 2");

            PermanentAssignment permAssign = new PermanentAssignment(user, adminRole, metadata1);
            assignmentManager.add(permAssign);

            System.out.println("Assignments count: " + assignmentManager.count());

            // Check user has role
            boolean hasRole = assignmentManager.userHasRole(user, adminRole);
            System.out.println("User has Administrator role: " + hasRole);

            // Check user permissions
            Set<Permission> userPerms = assignmentManager.getUserPermissions(user);
            System.out.println("User permissions count: " + userPerms.size());

            // Check specific permission
            boolean hasPerm = assignmentManager.userHasPermission(user, "WRITE", "users");
            System.out.println("User has WRITE on users: " + hasPerm);

            // Get active assignments
            List<RoleAssignment> active = assignmentManager.getActiveAssignments();
            System.out.println("Active assignments: " + active.size());

            // Try to add duplicate (should fail)
            try {
                PermanentAssignment duplicate = new PermanentAssignment(user, adminRole, metadata2);
                assignmentManager.add(duplicate);
                System.out.println("ERROR: Should have thrown exception for duplicate");
            } catch (IllegalArgumentException e) {
                System.out.println("Correctly rejected duplicate assignment: " + e.getMessage());
            }

            // Revoke assignment
            assignmentManager.revokeAssignment(permAssign.assignmentId());
            boolean hasRoleAfterRevoke = assignmentManager.userHasRole(user, adminRole);
            System.out.println("User has role after revoke: " + hasRoleAfterRevoke);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}