import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для AssignmentManager.
 */
class AssignmentManagerTest {

    private AssignmentManager assignmentManager;
    private UserManager userManager;
    private RoleManager roleManager;
    private User testUser;
    private Role adminRole;
    private Role viewerRole;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        
        testUser = User.validate("test_user", "Test User", "test@example.com");
        adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(new Permission("READ", "users", "Read users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Write users"));
        
        viewerRole = new Role("Viewer", "Read only");
        viewerRole.addPermission(new Permission("READ", "users", "Read users"));

        userManager.add(testUser);
        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        assignmentManager = new AssignmentManager(userManager, roleManager);
    }

    @Test
    @DisplayName("Создание постоянного назначения")
    void testAddPermanentAssignment() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, adminRole, metadata);
        
        assignmentManager.add(assignment);
        
        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(testUser, adminRole));
    }

    @Test
    @DisplayName("Создание временного назначения")
    void testAddTemporaryAssignment() {
        String futureDate = java.time.LocalDateTime.now().plusHours(1).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        TemporaryAssignment assignment = new TemporaryAssignment(testUser, viewerRole, metadata, futureDate);
        
        assignmentManager.add(assignment);
        
        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(testUser, viewerRole));
    }

    @Test
    @DisplayName("Попытка дублирования назначения должна выбрасывать исключение")
    void testDuplicateAssignment() {
        AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.now("admin", "Test 2");
        
        PermanentAssignment assignment1 = new PermanentAssignment(testUser, adminRole, metadata1);
        assignmentManager.add(assignment1);
        
        PermanentAssignment assignment2 = new PermanentAssignment(testUser, adminRole, metadata2);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment2));
    }

    @Test
    @DisplayName("Получение прав пользователя")
    void testGetUserPermissions() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, adminRole, metadata);
        assignmentManager.add(assignment);
        
        Set<Permission> permissions = assignmentManager.getUserPermissions(testUser);
        
        assertEquals(2, permissions.size());
        assertTrue(assignmentManager.userHasPermission(testUser, "READ", "users"));
        assertTrue(assignmentManager.userHasPermission(testUser, "WRITE", "users"));
    }

    @Test
    @DisplayName("Проверка отсутствия права у пользователя")
    void testUserDoesNotHavePermission() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, viewerRole, metadata);
        assignmentManager.add(assignment);
        
        assertFalse(assignmentManager.userHasPermission(testUser, "WRITE", "users"));
        assertTrue(assignmentManager.userHasPermission(testUser, "READ", "users"));
    }

    @Test
    @DisplayName("Отзыв постоянного назначения")
    void testRevokeAssignment() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, adminRole, metadata);
        assignmentManager.add(assignment);
        
        assertTrue(assignmentManager.userHasRole(testUser, adminRole));
        
        assignmentManager.revokeAssignment(assignment.assignmentId());
        
        assertFalse(assignmentManager.userHasRole(testUser, adminRole));
    }

    @Test
    @DisplayName("Продление временного назначения")
    void testExtendTemporaryAssignment() {
        String futureDate = java.time.LocalDateTime.now().plusHours(1).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        TemporaryAssignment assignment = new TemporaryAssignment(testUser, viewerRole, metadata, futureDate);
        assignmentManager.add(assignment);
        
        String newDate = java.time.LocalDateTime.now().plusHours(5).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), newDate);
        
        assertTrue(assignment.isActive());
    }

    @Test
    @DisplayName("Получение активных назначений")
    void testGetActiveAssignments() {
        AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test 1");
        PermanentAssignment assignment1 = new PermanentAssignment(testUser, adminRole, metadata1);
        assignmentManager.add(assignment1);
        
        AssignmentMetadata metadata2 = AssignmentMetadata.now("admin", "Test 2");
        PermanentAssignment assignment2 = new PermanentAssignment(testUser, viewerRole, metadata2);
        assignmentManager.add(assignment2);
        
        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        assertEquals(2, active.size());
    }

    @Test
    @DisplayName("Фильтрация назначений по пользователю")
    void testFindByUser() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, adminRole, metadata);
        assignmentManager.add(assignment);
        
        List<RoleAssignment> userAssignments = assignmentManager.findByUser(testUser);
        assertEquals(1, userAssignments.size());
    }

    @Test
    @DisplayName("Фильтрация назначений по роли")
    void testFindByRole() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, adminRole, metadata);
        assignmentManager.add(assignment);
        
        List<RoleAssignment> roleAssignments = assignmentManager.findByRole(adminRole);
        assertEquals(1, roleAssignments.size());
    }

    @Test
    @DisplayName("Фильтрация назначений по типу")
    void testFilterByType() {
        AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test 1");
        PermanentAssignment permAssignment = new PermanentAssignment(testUser, adminRole, metadata1);
        assignmentManager.add(permAssignment);
        
        String futureDate = java.time.LocalDateTime.now().plusHours(1).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        AssignmentMetadata metadata2 = AssignmentMetadata.now("admin", "Test 2");
        TemporaryAssignment tempAssignment = new TemporaryAssignment(testUser, viewerRole, metadata2, futureDate);
        assignmentManager.add(tempAssignment);
        
        List<RoleAssignment> permanent = assignmentManager.findByFilter(AssignmentFilters.byType("PERMANENT"));
        assertEquals(1, permanent.size());
        
        List<RoleAssignment> temporary = assignmentManager.findByFilter(AssignmentFilters.byType("TEMPORARY"));
        assertEquals(1, temporary.size());
    }

    @Test
    @DisplayName("Комбинированный фильтр: активные и постоянные")
    void testCombinedFilter() {
        AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test 1");
        PermanentAssignment permAssignment = new PermanentAssignment(testUser, adminRole, metadata1);
        assignmentManager.add(permAssignment);
        
        AssignmentFilter combined = AssignmentFilters.activeOnly()
                .and(AssignmentFilters.byType("PERMANENT"));
        
        List<RoleAssignment> filtered = assignmentManager.findByFilter(combined);
        assertEquals(1, filtered.size());
    }

    @Test
    @DisplayName("Сортировка назначений по имени пользователя")
    void testSortByUsername() {
        User user1 = User.validate("alice", "Alice", "alice@example.com");
        User user2 = User.validate("bob", "Bob", "bob@example.com");
        userManager.add(user1);
        userManager.add(user2);
        
        AssignmentMetadata metadata1 = AssignmentMetadata.now("admin", "Test 1");
        AssignmentMetadata metadata2 = AssignmentMetadata.now("admin", "Test 2");
        
        PermanentAssignment assign1 = new PermanentAssignment(user2, adminRole, metadata1);
        PermanentAssignment assign2 = new PermanentAssignment(user1, viewerRole, metadata2);
        
        assignmentManager.add(assign1);
        assignmentManager.add(assign2);
        
        List<RoleAssignment> sorted = assignmentManager.findAll(null, AssignmentSorters.byUsername());
        assertEquals("alice", sorted.get(0).user().username());
        assertEquals("bob", sorted.get(1).user().username());
    }

    @Test
    @DisplayName("Удаление назначения")
    void testRemoveAssignment() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(testUser, adminRole, metadata);
        assignmentManager.add(assignment);
        
        assertTrue(assignmentManager.remove(assignment));
        assertEquals(0, assignmentManager.count());
        assertFalse(assignmentManager.userHasRole(testUser, adminRole));
    }
}
