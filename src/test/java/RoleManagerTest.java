import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для RoleManager.
 */
class RoleManagerTest {

    private RoleManager roleManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
    }

    @Test
    @DisplayName("Добавление роли")
    void testAddRole() {
        Role role = new Role("Administrator", "Full access");
        roleManager.add(role);

        assertEquals(1, roleManager.count());
        assertTrue(roleManager.exists("Administrator"));
    }

    @Test
    @DisplayName("Добавление дубликата роли должно выбрасывать исключение")
    void testAddDuplicateRole() {
        Role role1 = new Role("Administrator", "Full access");
        Role role2 = new Role("Administrator", "Duplicate admin");

        roleManager.add(role1);
        assertThrows(IllegalArgumentException.class, () -> roleManager.add(role2));
    }

    @Test
    @DisplayName("Поиск роли по имени")
    void testFindByName() {
        Role role = new Role("Administrator", "Full access");
        roleManager.add(role);

        Optional<Role> found = roleManager.findByName("Administrator");
        assertTrue(found.isPresent());
        assertEquals("Full access", found.get().getDescription());
    }

    @Test
    @DisplayName("Поиск роли по ID")
    void testFindById() {
        Role role = new Role("Administrator", "Full access");
        roleManager.add(role);

        Optional<Role> found = roleManager.findById(role.getId());
        assertTrue(found.isPresent());
        assertEquals("Administrator", found.get().getName());
    }

    @Test
    @DisplayName("Добавление права к роли")
    void testAddPermissionToRole() {
        Role role = new Role("Administrator", "Full access");
        roleManager.add(role);

        Permission permission = new Permission("READ", "users", "Can read users");
        roleManager.addPermissionToRole("Administrator", permission);

        Optional<Role> updated = roleManager.findByName("Administrator");
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().getPermissions().size());
    }

    @Test
    @DisplayName("Удаление права из роли")
    void testRemovePermissionFromRole() {
        Role role = new Role("Administrator", "Full access");
        Permission permission = new Permission("READ", "users", "Can read users");
        role.addPermission(permission);
        roleManager.add(role);

        roleManager.removePermissionFromRole("Administrator", permission);

        Optional<Role> updated = roleManager.findByName("Administrator");
        assertTrue(updated.isPresent());
        assertEquals(0, updated.get().getPermissions().size());
    }

    @Test
    @DisplayName("Поиск ролей с указанным правом")
    void testFindRolesWithPermission() {
        Role adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(new Permission("READ", "users", "Can read users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Can write users"));

        Role viewerRole = new Role("Viewer", "Read only");
        viewerRole.addPermission(new Permission("READ", "users", "Can read users"));

        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        List<Role> rolesWithWrite = roleManager.findRolesWithPermission("WRITE", "users");
        assertEquals(1, rolesWithWrite.size());
        assertEquals("Administrator", rolesWithWrite.get(0).getName());
    }

    @Test
    @DisplayName("Фильтрация ролей по количеству прав")
    void testFilterByPermissionCount() {
        Role adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(new Permission("READ", "users", "Read"));
        adminRole.addPermission(new Permission("WRITE", "users", "Write"));
        adminRole.addPermission(new Permission("DELETE", "users", "Delete"));

        Role viewerRole = new Role("Viewer", "Read only");
        viewerRole.addPermission(new Permission("READ", "users", "Read"));

        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        List<Role> filtered = roleManager.findByFilter(RoleFilters.hasAtLeastNPermissions(2));
        assertEquals(1, filtered.size());
        assertEquals("Administrator", filtered.get(0).getName());
    }

    @Test
    @DisplayName("Сортировка ролей по имени")
    void testSortRolesByName() {
        roleManager.add(new Role("Manager", "Manager role"));
        roleManager.add(new Role("Admin", "Admin role"));
        roleManager.add(new Role("Viewer", "Viewer role"));

        List<Role> sorted = roleManager.findAll(null, RoleSorters.byName());
        assertEquals("Admin", sorted.get(0).getName());
        assertEquals("Manager", sorted.get(1).getName());
        assertEquals("Viewer", sorted.get(2).getName());
    }

    @Test
    @DisplayName("Сортировка ролей по количеству прав")
    void testSortRolesByPermissionCount() {
        Role role1 = new Role("One Perm", "One permission");
        role1.addPermission(new Permission("READ", "users", "Read"));

        Role role3 = new Role("Three Perms", "Three permissions");
        role3.addPermission(new Permission("READ", "users", "Read"));
        role3.addPermission(new Permission("WRITE", "users", "Write"));
        role3.addPermission(new Permission("DELETE", "users", "Delete"));

        Role role2 = new Role("Two Perms", "Two permissions");
        role2.addPermission(new Permission("READ", "users", "Read"));
        role2.addPermission(new Permission("WRITE", "users", "Write"));

        roleManager.add(role1);
        roleManager.add(role2);
        roleManager.add(role3);

        List<Role> sorted = roleManager.findAll(null, RoleSorters.byPermissionCount());
        assertEquals(1, sorted.get(0).getPermissions().size());
        assertEquals(2, sorted.get(1).getPermissions().size());
        assertEquals(3, sorted.get(2).getPermissions().size());
    }

    @Test
    @DisplayName("Удаление роли")
    void testRemoveRole() {
        Role role = new Role("Administrator", "Full access");
        roleManager.add(role);

        assertTrue(roleManager.remove(role));
        assertEquals(0, roleManager.count());
        assertFalse(roleManager.exists("Administrator"));
    }
}
