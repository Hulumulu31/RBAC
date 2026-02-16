import java.util.*;
import java.util.stream.Collectors;

/**
 * Интерактивная консольная утилита для управления пользователями, ролями и правами доступа
 * с использованием модели RBAC (Role-Based Access Control).
 */
public class RBACConsoleApp {
    private Map<String, User> users;
    private Map<String, Role> roles;
    private Map<String, RoleAssignment> assignments;
    private Scanner scanner;

    public RBACConsoleApp() {
        this.users = new HashMap<>();
        this.roles = new HashMap<>();
        this.assignments = new HashMap<>();
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
            users.put(username, user);
            System.out.println("User created successfully: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    private void editUser() {
        System.out.print("Enter username to edit: ");
        String username = scanner.nextLine().trim();

        User existingUser = users.get(username);
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
            User updatedUser = User.validate(username, newFullName, newEmail);
            users.put(username, updatedUser);
            System.out.println("User updated successfully: " + updatedUser.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        System.out.print("Enter username to delete: ");
        String username = scanner.nextLine().trim();

        User removedUser = users.remove(username);
        if (removedUser != null) {
            System.out.println("User deleted successfully: " + removedUser.format());

            // Also remove any assignments for this user
            List<String> assignmentsToRemove = assignments.entrySet().stream()
                    .filter(entry -> entry.getValue().user().username().equals(username))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            for (String assignId : assignmentsToRemove) {
                assignments.remove(assignId);
            }

            if (!assignmentsToRemove.isEmpty()) {
                System.out.println("Also removed " + assignmentsToRemove.size() + " assignment(s) for this user.");
            }
        } else {
            System.out.println("User not found: " + username);
        }
    }

    private void viewUser() {
        System.out.print("Enter username to view: ");
        String username = scanner.nextLine().trim();

        User user = users.get(username);
        if (user != null) {
            System.out.println("User details: " + user.format());
        } else {
            System.out.println("User not found: " + username);
        }
    }

    private void listAllUsers() {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("\n--- All Users ---");
        for (User user : users.values()) {
            System.out.println("- " + user.format());
        }
        System.out.println("Total users: " + users.size());
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
            roles.put(role.getId(), role);
            System.out.println("Role created successfully: " + role.getName() + " (ID: " + role.getId() + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating role: " + e.getMessage());
        }
    }

    private void editRole() {
        System.out.print("Enter role ID to edit: ");
        String roleId = scanner.nextLine().trim();

        Role existingRole = roles.get(roleId);
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
            Role updatedRole = new Role(newName, newDescription);
            updatedRole.getPermissions().forEach(updatedRole::addPermission); // Copy permissions
            roles.put(roleId, updatedRole);
            System.out.println("Role updated successfully: " + updatedRole.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating role: " + e.getMessage());
        }
    }

    private void deleteRole() {
        System.out.print("Enter role ID to delete: ");
        String roleId = scanner.nextLine().trim();

        Role removedRole = roles.remove(roleId);
        if (removedRole != null) {
            System.out.println("Role deleted successfully: " + removedRole.getName());

            // Also remove any assignments for this role
            List<String> assignmentsToRemove = assignments.entrySet().stream()
                    .filter(entry -> entry.getValue().role().getId().equals(roleId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            for (String assignId : assignmentsToRemove) {
                assignments.remove(assignId);
            }

            if (!assignmentsToRemove.isEmpty()) {
                System.out.println("Also removed " + assignmentsToRemove.size() + " assignment(s) for this role.");
            }
        } else {
            System.out.println("Role not found: " + roleId);
        }
    }

    private void viewRole() {
        System.out.print("Enter role ID to view: ");
        String roleId = scanner.nextLine().trim();

        Role role = roles.get(roleId);
        if (role != null) {
            System.out.println(role.format());
        } else {
            System.out.println("Role not found: " + roleId);
        }
    }

    private void listAllRoles() {
        if (roles.isEmpty()) {
            System.out.println("No roles found.");
            return;
        }

        System.out.println("\n--- All Roles ---");
        for (Role role : roles.values()) {
            System.out.println(role.format());
            System.out.println("---");
        }
        System.out.println("Total roles: " + roles.size());
    }

    private void addPermissionToRole() {
        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roles.get(roleId);
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

        Role role = roles.get(roleId);
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

        User user = users.get(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return;
        }

        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roles.get(roleId);
        if (role == null) {
            System.out.println("Role not found: " + roleId);
            return;
        }

        System.out.print("Enter assigned by (username): ");
        String assignedBy = scanner.nextLine().trim();

        System.out.print("Enter reason for assignment: ");
        String reason = scanner.nextLine().trim();

        AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);

        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignments.put(assignment.assignmentId(), assignment);

        System.out.println("Permanent assignment created: " + assignment.summary());
    }

    private void createTemporaryAssignment() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        User user = users.get(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return;
        }

        System.out.print("Enter role ID: ");
        String roleId = scanner.nextLine().trim();

        Role role = roles.get(roleId);
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

        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata, expiresAt);
        assignments.put(assignment.assignmentId(), assignment);

        System.out.println("Temporary assignment created: " + assignment.summary());
    }

    private void revokePermanentAssignment() {
        System.out.print("Enter assignment ID to revoke: ");
        String assignmentId = scanner.nextLine().trim();

        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            System.out.println("Assignment not found: " + assignmentId);
            return;
        }

        if (assignment instanceof PermanentAssignment) {
            PermanentAssignment permAssignment = (PermanentAssignment) assignment;
            permAssignment.revoke();
            System.out.println("Permanent assignment revoked: " + assignment.summary());
        } else {
            System.out.println("Assignment is not a permanent assignment and cannot be revoked this way.");
        }
    }

    private void extendTemporaryAssignment() {
        System.out.print("Enter assignment ID to extend: ");
        String assignmentId = scanner.nextLine().trim();

        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            System.out.println("Assignment not found: " + assignmentId);
            return;
        }

        if (assignment instanceof TemporaryAssignment) {
            TemporaryAssignment tempAssignment = (TemporaryAssignment) assignment;
            
            System.out.print("Enter new expiration date (yyyy-MM-dd HH:mm:ss): ");
            String newExpiresAt = scanner.nextLine().trim();
            
            tempAssignment.extend(newExpiresAt);
            System.out.println("Temporary assignment extended: " + tempAssignment.summary());
        } else {
            System.out.println("Assignment is not a temporary assignment and cannot be extended.");
        }
    }

    private void viewAssignment() {
        System.out.print("Enter assignment ID to view: ");
        String assignmentId = scanner.nextLine().trim();

        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment != null) {
            System.out.println(assignment.summary());
        } else {
            System.out.println("Assignment not found: " + assignmentId);
        }
    }

    private void listAllAssignments() {
        if (assignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        System.out.println("\n--- All Assignments ---");
        for (RoleAssignment assignment : assignments.values()) {
            System.out.println(assignment.summary());
            System.out.println("---");
        }
        System.out.println("Total assignments: " + assignments.size());
    }

    private void searchAndFilter() {
        System.out.println("\n--- Search and Filter ---");
        System.out.println("1. Search Users");
        System.out.println("2. Search Roles");
        System.out.println("3. Search Assignments");
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
            case 0:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void searchUsers() {
        System.out.print("Enter search term for users: ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        List<User> matchingUsers = users.values().stream()
                .filter(user -> user.username().toLowerCase().contains(searchTerm) ||
                               user.fullName().toLowerCase().contains(searchTerm) ||
                               user.email().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

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

        List<Role> matchingRoles = roles.values().stream()
                .filter(role -> role.getName().toLowerCase().contains(searchTerm) ||
                               role.getDescription().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

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

        List<RoleAssignment> matchingAssignments = assignments.values().stream()
                .filter(assignment -> assignment.user().username().toLowerCase().contains(searchTerm) ||
                                     assignment.role().getName().toLowerCase().contains(searchTerm) ||
                                     assignment.metadata().assignedBy().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

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
        System.out.println("Total users: " + users.size());
        
        if (!users.isEmpty()) {
            // Find most common domain in emails
            Map<String, Long> domainCount = users.values().stream()
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
        System.out.println("Total roles: " + roles.size());
        
        if (!roles.isEmpty()) {
            // Find role with most permissions
            Role largestRole = roles.values().stream()
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
        System.out.println("Total assignments: " + assignments.size());
        
        long permanentCount = assignments.values().stream()
                .filter(assignment -> assignment instanceof PermanentAssignment)
                .count();
                
        long temporaryCount = assignments.values().stream()
                .filter(assignment -> assignment instanceof TemporaryAssignment)
                .count();
                
        System.out.println("Permanent assignments: " + permanentCount);
        System.out.println("Temporary assignments: " + temporaryCount);
        
        long activeCount = assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .count();
                
        System.out.println("Active assignments: " + activeCount);
        System.out.println("Inactive assignments: " + (assignments.size() - activeCount));
    }

    private void permissionsByRole() {
        System.out.println("\n--- Permissions by Role ---");
        if (roles.isEmpty()) {
            System.out.println("No roles found.");
            return;
        }
        
        for (Role role : roles.values()) {
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
                System.out.println("Users: " + users.size() + ", Roles: " + roles.size() + ", Assignments: " + assignments.size());
                break;
            case 2:
                System.out.println("Data loaded successfully from file (simulation).");
                System.out.println("Current counts - Users: " + users.size() + ", Roles: " + roles.size() + ", Assignments: " + assignments.size());
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
}