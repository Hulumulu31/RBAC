import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для UserManager.
 */
class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    @DisplayName("Добавление пользователя")
    void testAddUser() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("john_doe"));
    }

    @Test
    @DisplayName("Добавление дубликата пользователя должно выбрасывать исключение")
    void testAddDuplicateUser() {
        User user1 = User.validate("john_doe", "John Doe", "john@example.com");
        User user2 = User.validate("john_doe", "John Doe 2", "john2@example.com");

        userManager.add(user1);
        assertThrows(IllegalArgumentException.class, () -> userManager.add(user2));
    }

    @Test
    @DisplayName("Поиск пользователя по username")
    void testFindByUsername() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        Optional<User> found = userManager.findByUsername("john_doe");
        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().email());
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void testFindByEmail() {
        User user1 = User.validate("john_doe", "John Doe", "john@company.com");
        User user2 = User.validate("jane_smith", "Jane Smith", "jane@example.com");
        userManager.add(user1);
        userManager.add(user2);

        Optional<User> found = userManager.findByEmail("john@company.com");
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().username());
    }

    @Test
    @DisplayName("Фильтрация пользователей по домену email")
    void testFilterByDomain() {
        userManager.add(User.validate("user1", "User One", "user1@company.com"));
        userManager.add(User.validate("user2", "User Two", "user2@example.com"));
        userManager.add(User.validate("user3", "User Three", "user3@company.com"));

        List<User> filtered = userManager.findByFilter(UserFilters.byEmailDomain("@company.com"));
        assertEquals(2, filtered.size());
    }

    @Test
    @DisplayName("Комбинирование фильтров AND")
    void testCombinedFiltersAnd() {
        userManager.add(User.validate("john_doe", "John Doe", "john@company.com"));
        userManager.add(User.validate("john_smith", "John Smith", "john@example.com"));
        userManager.add(User.validate("jane_doe", "Jane Doe", "jane@company.com"));

        UserFilter combined = UserFilters.byUsernameContains("john")
                .and(UserFilters.byEmailDomain("@company.com"));

        List<User> filtered = userManager.findByFilter(combined);
        assertEquals(1, filtered.size());
        assertEquals("john_doe", filtered.get(0).username());
    }

    @Test
    @DisplayName("Сортировка пользователей по имени")
    void testSortUsers() {
        userManager.add(User.validate("charlie", "Charlie Brown", "charlie@example.com"));
        userManager.add(User.validate("alice", "Alice Wonder", "alice@example.com"));
        userManager.add(User.validate("bob", "Bob Builder", "bob@example.com"));

        List<User> sorted = userManager.findAll(null, UserSorters.byUsername());
        assertEquals("alice", sorted.get(0).username());
        assertEquals("bob", sorted.get(1).username());
        assertEquals("charlie", sorted.get(2).username());
    }

    @Test
    @DisplayName("Обновление пользователя")
    void testUpdateUser() {
        userManager.add(User.validate("john_doe", "John Doe", "john@example.com"));
        userManager.update("john_doe", "John Updated", "john.updated@example.com");

        Optional<User> updated = userManager.findByUsername("john_doe");
        assertTrue(updated.isPresent());
        assertEquals("John Updated", updated.get().fullName());
        assertEquals("john.updated@example.com", updated.get().email());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testRemoveUser() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        assertTrue(userManager.remove(user));
        assertEquals(0, userManager.count());
        assertFalse(userManager.exists("john_doe"));
    }
}
