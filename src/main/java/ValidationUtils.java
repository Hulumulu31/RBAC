import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Утилитный класс для валидации данных.
 * Содержит статические методы для проверки корректности ввода.
 */
public class ValidationUtils {

    // Regex паттерны для валидации
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    /**
     * Проверяет корректность имени пользователя.
     * Требования:
     * - Длина от 3 до 20 символов
     * - Первый символ - буква
     * - Разрешены буквы, цифры и подчеркивание
     *
     * @param username имя пользователя для проверки
     * @return true, если имя пользователя корректно
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Проверяет корректность email адреса.
     * Требования:
     * - Содержит символ @
     * - Содержит домен с точкой
     * - Соответствует стандартному формату email
     *
     * @param email email для проверки
     * @return true, если email корректен
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Проверяет формат даты.
     * Поддерживаемые форматы:
     * - "YYYY-MM-DD"
     * - "YYYY-MM-DD HH:MM:SS"
     *
     * @param date строка даты для проверки
     * @return true, если формат даты корректен
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        String trimmedDate = date.trim();
        return DATE_PATTERN.matcher(trimmedDate).matches() ||
               DATETIME_PATTERN.matcher(trimmedDate).matches();
    }

    /**
     * Нормализует строку: удаляет лишние пробелы и приводит к нижнему регистру.
     *
     * @param input входная строка
     * @return нормализованная строка
     */
    public static String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        // Удаляем лишние пробелы (в начале, в конце и множественные пробелы)
        return input.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Проверяет, что строка не пустая.
     *
     * @param value строка для проверки
     * @param fieldName имя поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка пустая
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Проверяет, что строка не null.
     *
     * @param value строка для проверки
     * @param fieldName имя поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка null
     */
    public static void requireNonNull(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Проверяет, что число находится в заданном диапазоне.
     *
     * @param value значение для проверки
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @param fieldName имя поля для сообщения об ошибке
     * @throws IllegalArgumentException если значение вне диапазона
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                fieldName + " must be between " + min + " and " + max + ", but was " + value);
        }
    }
}
