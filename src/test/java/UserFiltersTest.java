import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для фильтров пользователей (UserFilters).
 */
class UserFiltersTest {

    @Test
    @DisplayName("Фильтр по точному совпадению username")
    void testByUsername() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        UserFilter filter = UserFilters.byUsername("john_doe");
        
        assertTrue(filter.test(user));
        assertFalse(filter.test(User.validate("jane_doe", "Jane Doe", "jane@example.com")));
    }

    @Test
    @DisplayName("Фильтр по подстроке в username (игнорируя регистр)")
    void testByUsernameContains() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        UserFilter filter = UserFilters.byUsernameContains("john");
        
        assertTrue(filter.test(user));
        assertTrue(filter.test(User.validate("John_Doe", "John Doe", "john@example.com")));
        assertFalse(filter.test(User.validate("jane_doe", "Jane Doe", "jane@example.com")));
    }

    @Test
    @DisplayName("Фильтр по точному совпадению email")
    void testByEmail() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        UserFilter filter = UserFilters.byEmail("john@example.com");
        
        assertTrue(filter.test(user));
        assertFalse(filter.test(User.validate("john_doe", "John Doe", "john@other.com")));
    }

    @Test
    @DisplayName("Фильтр по домену email")
    void testByEmailDomain() {
        User user1 = User.validate("user1", "User One", "user1@company.com");
        User user2 = User.validate("user2", "User Two", "user2@example.com");
        
        UserFilter filter = UserFilters.byEmailDomain("@company.com");
        
        assertTrue(filter.test(user1));
        assertFalse(filter.test(user2));
    }

    @Test
    @DisplayName("Фильтр по подстроке в полном имени")
    void testByFullNameContains() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        UserFilter filter = UserFilters.byFullNameContains("John");
        
        assertTrue(filter.test(user));
        assertTrue(filter.test(User.validate("john_doe", "john doe", "john@example.com")));
        assertFalse(filter.test(User.validate("jane_doe", "Jane Smith", "jane@example.com")));
    }

    @Test
    @DisplayName("Комбинирование фильтров AND")
    void testAndFilter() {
        User user1 = User.validate("john_doe", "John Doe", "john@company.com");
        User user2 = User.validate("john_smith", "John Smith", "john@example.com");
        
        UserFilter filter = UserFilters.byUsernameContains("john")
                .and(UserFilters.byEmailDomain("@company.com"));
        
        assertTrue(filter.test(user1));
        assertFalse(filter.test(user2));
    }

    @Test
    @DisplayName("Комбинирование фильтров OR")
    void testOrFilter() {
        User user1 = User.validate("john_doe", "John Doe", "john@company.com");
        User user2 = User.validate("jane_doe", "Jane Doe", "jane@example.com");
        User user3 = User.validate("bob", "Bob", "bob@example.com");
        
        UserFilter filter = UserFilters.byUsernameContains("john")
                .or(UserFilters.byEmailDomain("@example.com"));
        
        assertTrue(filter.test(user1));
        assertTrue(filter.test(user2));
        assertTrue(filter.test(user3));
    }

    @Test
    @DisplayName("Комбинирование с null фильтром")
    void testCombinedWithNull() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        UserFilter filter = UserFilters.byUsername("john_doe");
        
        UserFilter andNull = filter.and(null);
        assertTrue(andNull.test(user));
        
        UserFilter orNull = filter.or(null);
        assertTrue(orNull.test(user));
    }
}
