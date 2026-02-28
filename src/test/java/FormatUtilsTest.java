import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Тесты для класса FormatUtils.
 */
class FormatUtilsTest {

    @Test
    @DisplayName("truncate - обрезка строк")
    void testTruncate() {
        assertEquals("hello", FormatUtils.truncate("hello", 10));
        assertEquals("he...", FormatUtils.truncate("hello world", 5));
        assertEquals("hel", FormatUtils.truncate("hello", 3)); // при длине <= 3 просто обрезает
        assertEquals("he", FormatUtils.truncate("hello", 2));
        assertEquals("", FormatUtils.truncate("", 5));
        assertEquals("", FormatUtils.truncate(null, 5));
    }

    @Test
    @DisplayName("padRight - дополнение справа")
    void testPadRight() {
        assertEquals("hello     ", FormatUtils.padRight("hello", 10));
        assertEquals("hello", FormatUtils.padRight("hello", 5));
        assertEquals("hel", FormatUtils.padRight("hello", 3));
        assertEquals("          ", FormatUtils.padRight("", 10));
        assertEquals("          ", FormatUtils.padRight(null, 10));
    }

    @Test
    @DisplayName("padLeft - дополнение слева")
    void testPadLeft() {
        assertEquals("     hello", FormatUtils.padLeft("hello", 10));
        assertEquals("hello", FormatUtils.padLeft("hello", 5));
        assertEquals("hel", FormatUtils.padLeft("hello", 3));
        assertEquals("          ", FormatUtils.padLeft("", 10));
        assertEquals("          ", FormatUtils.padLeft(null, 10));
    }

    @Test
    @DisplayName("formatBox - форматирование рамки")
    void testFormatBox() {
        String result = FormatUtils.formatBox("Hello");
        assertTrue(result.contains("+-------+"));
        assertTrue(result.contains("| Hello |"));
    }

    @Test
    @DisplayName("formatBox - пустая строка")
    void testFormatBox_empty() {
        String result = FormatUtils.formatBox("");
        assertTrue(result.contains("+--+"));
    }

    @Test
    @DisplayName("formatHeader - форматирование заголовка")
    void testFormatHeader() {
        String result = FormatUtils.formatHeader("test");
        assertTrue(result.contains("===="));
        assertTrue(result.contains("TEST"));
    }

    @Test
    @DisplayName("formatHeader - null и пустая строка")
    void testFormatHeader_empty() {
        assertEquals("", FormatUtils.formatHeader(""));
        assertEquals("", FormatUtils.formatHeader(null));
    }

    @Test
    @DisplayName("formatTable - простая таблица")
    void testFormatTable() {
        String[] headers = {"Name", "Age"};
        List<String[]> rows = java.util.Arrays.asList(
            new String[]{"John", "25"},
            new String[]{"Jane", "30"}
        );
        
        String result = FormatUtils.formatTable(headers, rows);
        
        assertTrue(result.contains("|"));
        assertTrue(result.contains("+"));
        assertTrue(result.contains("Name"));
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Jane"));
    }

    @Test
    @DisplayName("formatTable - пустые заголовки")
    void testFormatTable_emptyHeaders() {
        String result = FormatUtils.formatTable(new String[]{}, java.util.Collections.emptyList());
        assertEquals("No data to display", result);
    }

    @Test
    @DisplayName("padCenter - центрирование")
    void testPadCenter() {
        assertEquals("   hello   ", FormatUtils.padCenter("hello", 11));
        assertEquals("hello", FormatUtils.padCenter("hello", 5));
        assertEquals("hel", FormatUtils.padCenter("hello", 3));
    }

    @Test
    @DisplayName("createLine - создание линии")
    void testCreateLine() {
        assertEquals("-----", FormatUtils.createLine(5, '-'));
        assertEquals("=====", FormatUtils.createLine(5, '='));
        assertEquals("", FormatUtils.createLine(0, '-'));
    }

    @Test
    @DisplayName("formatNumber - форматирование числа")
    void testFormatNumber() {
        // Проверка, что число отформатировано (содержит разделитель)
        String formatted = FormatUtils.formatNumber(1000);
        assertTrue(formatted.length() >= 4); // 1000 -> как минимум 4 символа
        assertTrue(formatted.contains("000"));
    }
}
