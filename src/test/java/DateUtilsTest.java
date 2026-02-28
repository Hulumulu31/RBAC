import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса DateUtils.
 */
class DateUtilsTest {

    @Test
    @DisplayName("getCurrentDate - формат даты")
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    @DisplayName("getCurrentDateTime - формат даты и времени")
    void testGetCurrentDateTime() {
        String dateTime = DateUtils.getCurrentDateTime();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("isBefore - сравнение дат")
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2024-01-01", "2024-12-31"));
        assertTrue(DateUtils.isBefore("2024-01-01 00:00:00", "2024-12-31 23:59:59"));
        assertFalse(DateUtils.isBefore("2024-12-31", "2024-01-01"));
        assertFalse(DateUtils.isBefore("2024-06-15", "2024-06-15"));
        assertFalse(DateUtils.isBefore(null, "2024-01-01"));
        assertFalse(DateUtils.isBefore("2024-01-01", null));
    }

    @Test
    @DisplayName("isAfter - сравнение дат")
    void testIsAfter() {
        assertTrue(DateUtils.isAfter("2024-12-31", "2024-01-01"));
        assertTrue(DateUtils.isAfter("2024-12-31 23:59:59", "2024-01-01 00:00:00"));
        assertFalse(DateUtils.isAfter("2024-01-01", "2024-12-31"));
        assertFalse(DateUtils.isAfter("2024-06-15", "2024-06-15"));
        assertFalse(DateUtils.isAfter(null, "2024-01-01"));
        assertFalse(DateUtils.isAfter("2024-01-01", null));
    }

    @Test
    @DisplayName("isEqual - равенство дат")
    void testIsEqual() {
        assertTrue(DateUtils.isEqual("2024-06-15", "2024-06-15"));
        assertTrue(DateUtils.isEqual("2024-06-15 10:30:00", "2024-06-15 10:30:00"));
        assertFalse(DateUtils.isEqual("2024-01-01", "2024-12-31"));
        assertFalse(DateUtils.isEqual(null, "2024-01-01"));
    }

    @Test
    @DisplayName("addDays - добавление дней")
    void testAddDays() {
        assertEquals("2024-01-16", DateUtils.addDays("2024-01-15", 1));
        assertEquals("2024-01-14", DateUtils.addDays("2024-01-15", -1));
        assertEquals("2024-01-25", DateUtils.addDays("2024-01-15", 10));
        assertEquals("2024-02-15", DateUtils.addDays("2024-01-15", 31));
        
        assertEquals("2024-01-16 10:30:00", DateUtils.addDays("2024-01-15 10:30:00", 1));
    }

    @Test
    @DisplayName("addDays - исключения")
    void testAddDays_invalid() {
        assertThrows(IllegalArgumentException.class, 
            () -> DateUtils.addDays(null, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> DateUtils.addDays("", 1));
        assertThrows(IllegalArgumentException.class, 
            () -> DateUtils.addDays("invalid-date", 1));
    }

    @Test
    @DisplayName("daysBetween - количество дней между датами")
    void testDaysBetween() {
        assertEquals(10, DateUtils.daysBetween("2024-01-01", "2024-01-11"));
        assertEquals(-10, DateUtils.daysBetween("2024-01-11", "2024-01-01"));
        assertEquals(0, DateUtils.daysBetween("2024-01-01", "2024-01-01"));
        assertEquals(0, DateUtils.daysBetween(null, "2024-01-01"));
    }

    @Test
    @DisplayName("formatRelativeTime - относительное время (будущее)")
    void testFormatRelativeTime_future() {
        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 5);
        String result = DateUtils.formatRelativeTime(futureDate);
        assertTrue(result.contains("day") || result.contains("from now"));
    }

    @Test
    @DisplayName("formatRelativeTime - относительное время (прошлое)")
    void testFormatRelativeTime_past() {
        String pastDate = DateUtils.addDays(DateUtils.getCurrentDate(), -5);
        String result = DateUtils.formatRelativeTime(pastDate);
        assertTrue(result.contains("day") || result.contains("ago"));
    }

    @Test
    @DisplayName("formatRelativeTime - некорректная дата")
    void testFormatRelativeTime_invalid() {
        assertEquals("Invalid date format", DateUtils.formatRelativeTime("invalid"));
        assertEquals("Invalid date", DateUtils.formatRelativeTime(null));
    }

    @Test
    @DisplayName("isExpired - проверка истечения срока")
    void testIsExpired() {
        assertFalse(DateUtils.isExpired(DateUtils.addDays(DateUtils.getCurrentDate(), 1)));
        assertTrue(DateUtils.isExpired(DateUtils.addDays(DateUtils.getCurrentDate(), -1)));
        assertTrue(DateUtils.isExpired(null));
        assertTrue(DateUtils.isExpired(""));
    }

    @Test
    @DisplayName("isActive - проверка активности")
    void testIsActive() {
        assertTrue(DateUtils.isActive(DateUtils.addDays(DateUtils.getCurrentDate(), 1)));
        assertFalse(DateUtils.isActive(DateUtils.addDays(DateUtils.getCurrentDate(), -1)));
    }

    @Test
    @DisplayName("extractDate - извлечение даты")
    void testExtractDate() {
        assertEquals("2024-01-15", DateUtils.extractDate("2024-01-15 10:30:00"));
        assertEquals("2024-01-15", DateUtils.extractDate("2024-01-15"));
        assertEquals("", DateUtils.extractDate(""));
        assertEquals("", DateUtils.extractDate(null));
    }

    @Test
    @DisplayName("extractTime - извлечение времени")
    void testExtractTime() {
        assertEquals("10:30:00", DateUtils.extractTime("2024-01-15 10:30:00"));
        assertEquals("00:00:00", DateUtils.extractTime("2024-01-15"));
        assertEquals("", DateUtils.extractTime(""));
        assertEquals("", DateUtils.extractTime(null));
    }

    @Test
    @DisplayName("formatDate - форматирование даты")
    void testFormatDate() {
        assertEquals("01/15/2024", DateUtils.formatDate("2024-01-15", "MM/dd/yyyy"));
        assertEquals("15.01.2024", DateUtils.formatDate("2024-01-15", "dd.MM.yyyy"));
        assertEquals("Invalid date", DateUtils.formatDate("invalid", "MM/dd/yyyy"));
        assertEquals("", DateUtils.formatDate(null, "MM/dd/yyyy"));
    }
}
