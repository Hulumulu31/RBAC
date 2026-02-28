import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса ValidationUtils.
 */
class ValidationUtilsTest {

    @Test
    @DisplayName("isValidUsername - корректные имена")
    void testValidUsername_validCases() {
        assertTrue(ValidationUtils.isValidUsername("john_doe"));
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("admin"));
        assertTrue(ValidationUtils.isValidUsername("test_user_1"));
        assertTrue(ValidationUtils.isValidUsername("a1_"));
    }

    @Test
    @DisplayName("isValidUsername - некорректные имена")
    void testValidUsername_invalidCases() {
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername("ab")); // слишком короткое
        assertFalse(ValidationUtils.isValidUsername("user-name")); // дефис
        assertFalse(ValidationUtils.isValidUsername("user@name")); // @
        assertFalse(ValidationUtils.isValidUsername("123user")); // начинается с цифры
        assertFalse(ValidationUtils.isValidUsername("user name")); // пробел
        assertFalse(ValidationUtils.isValidUsername(null));
        assertFalse(ValidationUtils.isValidUsername("   "));
    }

    @Test
    @DisplayName("isValidEmail - корректные email")
    void testValidEmail_validCases() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@company.org"));
        assertTrue(ValidationUtils.isValidEmail("admin@test.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("user+tag@gmail.com"));
        assertTrue(ValidationUtils.isValidEmail("user123@test.com"));
    }

    @Test
    @DisplayName("isValidEmail - некорректные email")
    void testValidEmail_invalidCases() {
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("invalid"));
        assertFalse(ValidationUtils.isValidEmail("no@dot"));
        assertFalse(ValidationUtils.isValidEmail("@nodomain.com"));
        assertFalse(ValidationUtils.isValidEmail("spaces in@email.com"));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    @DisplayName("isValidDate - корректные даты")
    void testValidDate_validCases() {
        assertTrue(ValidationUtils.isValidDate("2024-01-15"));
        assertTrue(ValidationUtils.isValidDate("2024-12-31"));
        assertTrue(ValidationUtils.isValidDate("2024-01-15 10:30:00"));
        assertTrue(ValidationUtils.isValidDate("2024-06-20 23:59:59"));
    }

    @Test
    @DisplayName("isValidDate - некорректные даты")
    void testValidDate_invalidCases() {
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate("15-01-2024"));
        assertFalse(ValidationUtils.isValidDate("2024/01/15"));
        assertFalse(ValidationUtils.isValidDate("01.15.2024"));
        assertFalse(ValidationUtils.isValidDate("not-a-date"));
        assertFalse(ValidationUtils.isValidDate(null));
    }

    @Test
    @DisplayName("normalizeString - нормализация строк")
    void testNormalizeString() {
        assertEquals("hello world", ValidationUtils.normalizeString("  hello   world  "));
        assertEquals("test", ValidationUtils.normalizeString("  TEST  "));
        assertEquals("", ValidationUtils.normalizeString(""));
        assertEquals("", ValidationUtils.normalizeString(null));
        assertEquals("", ValidationUtils.normalizeString("   "));
        assertEquals("one two three", ValidationUtils.normalizeString("one  two   three"));
    }

    @Test
    @DisplayName("requireNonEmpty - корректные значения")
    void testRequireNonEmpty_validCases() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("test", "field"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("  test  ", "field"));
    }

    @Test
    @DisplayName("requireNonEmpty - пустые значения")
    void testRequireNonEmpty_invalidCases() {
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtils.requireNonEmpty("", "field"));
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtils.requireNonEmpty("   ", "field"));
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtils.requireNonEmpty(null, "field"));
    }

    @Test
    @DisplayName("requireInRange - корректные значения")
    void testRequireInRange_validCases() {
        assertDoesNotThrow(() -> ValidationUtils.requireInRange(5, 1, 10, "value"));
        assertDoesNotThrow(() -> ValidationUtils.requireInRange(1, 1, 10, "value"));
        assertDoesNotThrow(() -> ValidationUtils.requireInRange(10, 1, 10, "value"));
    }

    @Test
    @DisplayName("requireInRange - значения вне диапазона")
    void testRequireInRange_invalidCases() {
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtils.requireInRange(0, 1, 10, "value"));
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtils.requireInRange(11, 1, 10, "value"));
    }
}
