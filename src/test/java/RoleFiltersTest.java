import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для фильтров ролей (RoleFilters).
 */
class RoleFiltersTest {

    @Test
    @DisplayName("Фильтр по точному совпадению имени роли")
    void testByName() {
        Role role = new Role("Administrator", "Full access");
        RoleFilter filter = RoleFilters.byName("Administrator");
        
        assertTrue(filter.test(role));
        assertFalse(filter.test(new Role("Admin", "Admin")));
    }

    @Test
    @DisplayName("Фильтр по подстроке в имени роли")
    void testByNameContains() {
        Role role = new Role("Administrator", "Full access");
        RoleFilter filter = RoleFilters.byNameContains("admin");
        
        assertTrue(filter.test(role));
        assertTrue(filter.test(new Role("SuperAdmin", "Super")));
        assertFalse(filter.test(new Role("Viewer", "Read only")));
    }

    @Test
    @DisplayName("Фильтр по наличию права")
    void testHasPermission() {
        Role role = new Role("Administrator", "Full access");
        Permission readPerm = new Permission("READ", "users", "Read users");
        role.addPermission(readPerm);
        
        RoleFilter filter = RoleFilters.hasPermission(readPerm);
        
        assertTrue(filter.test(role));
        assertFalse(filter.test(new Role("Viewer", "Read only")));
    }

    @Test
    @DisplayName("Фильтр по имени права и ресурсу")
    void testHasPermissionByNameAndResource() {
        Role adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(new Permission("READ", "users", "Read users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Write users"));
        
        Role viewerRole = new Role("Viewer", "Read only");
        viewerRole.addPermission(new Permission("READ", "users", "Read users"));
        
        RoleFilter filter = RoleFilters.hasPermission("WRITE", "users");
        
        assertTrue(filter.test(adminRole));
        assertFalse(filter.test(viewerRole));
    }

    @Test
    @DisplayName("Фильтр по минимальному количеству прав")
    void testHasAtLeastNPermissions() {
        Role role1 = new Role("One Perm", "One permission");
        role1.addPermission(new Permission("READ", "users", "Read"));
        
        Role role2 = new Role("Two Perms", "Two permissions");
        role2.addPermission(new Permission("READ", "users", "Read"));
        role2.addPermission(new Permission("WRITE", "users", "Write"));
        
        Role role3 = new Role("Three Perms", "Three permissions");
        role3.addPermission(new Permission("READ", "users", "Read"));
        role3.addPermission(new Permission("WRITE", "users", "Write"));
        role3.addPermission(new Permission("DELETE", "users", "Delete"));
        
        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(2);
        
        assertFalse(filter.test(role1));
        assertTrue(filter.test(role2));
        assertTrue(filter.test(role3));
    }

    @Test
    @DisplayName("Комбинирование фильтров AND")
    void testAndFilter() {
        Role adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(new Permission("READ", "users", "Read"));
        adminRole.addPermission(new Permission("WRITE", "users", "Write"));
        
        Role viewerRole = new Role("Admin Viewer", "Read only");
        viewerRole.addPermission(new Permission("READ", "users", "Read"));
        
        RoleFilter filter = RoleFilters.byNameContains("Admin")
                .and(RoleFilters.hasAtLeastNPermissions(2));
        
        assertTrue(filter.test(adminRole));
        assertFalse(filter.test(viewerRole));
    }

    @Test
    @DisplayName("Комбинирование фильтров OR")
    void testOrFilter() {
        Role role1 = new Role("Administrator", "Full access");
        Role role2 = new Role("Viewer", "Read only");
        Role role3 = new Role("Manager", "Manager");
        
        RoleFilter filter = RoleFilters.byName("Administrator")
                .or(RoleFilters.byName("Viewer"));
        
        assertTrue(filter.test(role1));
        assertTrue(filter.test(role2));
        assertFalse(filter.test(role3));
    }
}
