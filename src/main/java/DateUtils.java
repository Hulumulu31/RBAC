import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Утилитный класс для работы с датами.
 * Предоставляет методы для форматирования, сравнения и вычислений с датами.
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Возвращает текущую дату в формате "YYYY-MM-DD".
     *
     * @return текущая дата
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Возвращает текущую дату и время в формате "YYYY-MM-DD HH:MM:SS".
     *
     * @return текущая дата и время
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * Проверяет, находится ли дата1 перед датой2.
     *
     * @param date1 первая дата (в формате yyyy-MM-dd или yyyy-MM-dd HH:mm:ss)
     * @param date2 вторая дата (в формате yyyy-MM-dd или yyyy-MM-dd HH:mm:ss)
     * @return true, если date1 раньше date2
     */
    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        
        try {
            // Пробуем сравнить как строки (работает для формата YYYY-MM-DD)
            String d1 = normalizeDate(date1);
            String d2 = normalizeDate(date2);
            return d1.compareTo(d2) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверяет, находится ли дата1 после даты2.
     *
     * @param date1 первая дата (в формате yyyy-MM-dd или yyyy-MM-dd HH:mm:ss)
     * @param date2 вторая дата (в формате yyyy-MM-dd или yyyy-MM-dd HH:mm:ss)
     * @return true, если date1 позже date2
     */
    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        
        try {
            String d1 = normalizeDate(date1);
            String d2 = normalizeDate(date2);
            return d1.compareTo(d2) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверяет, равны ли две даты.
     *
     * @param date1 первая дата
     * @param date2 вторая дата
     * @return true, если даты равны
     */
    public static boolean isEqual(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        
        try {
            String d1 = normalizeDate(date1);
            String d2 = normalizeDate(date2);
            return d1.compareTo(d2) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Добавляет указанное количество дней к дате.
     *
     * @param date исходная дата (в формате yyyy-MM-dd или yyyy-MM-dd HH:mm:ss)
     * @param days количество дней для добавления (может быть отрицательным)
     * @return новая дата
     */
    public static String addDays(String date, int days) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        
        try {
            if (date.contains(" ")) {
                // Дата с временем
                LocalDateTime dateTime = LocalDateTime.parse(date.trim(), DATETIME_FORMATTER);
                return dateTime.plusDays(days).format(DATETIME_FORMATTER);
            } else {
                // Только дата
                LocalDate localDate = LocalDate.parse(date.trim(), DATE_FORMATTER);
                return localDate.plusDays(days).format(DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date, e);
        }
    }

    /**
     * Добавляет указанное количество часов к дате.
     *
     * @param date исходная дата с временем
     * @param hours количество часов для добавления
     * @return новая дата
     */
    public static String addHours(String date, int hours) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date.trim(), DATETIME_FORMATTER);
            return dateTime.plusHours(hours).format(DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date, e);
        }
    }

    /**
     * Вычисляет количество дней между двумя датами.
     *
     * @param date1 первая дата
     * @param date2 вторая дата
     * @return количество дней (положительное, если date2 позже date1)
     */
    public static long daysBetween(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return 0;
        }
        
        try {
            LocalDate d1 = LocalDate.parse(normalizeDate(date1).substring(0, 10), DATE_FORMATTER);
            LocalDate d2 = LocalDate.parse(normalizeDate(date2).substring(0, 10), DATE_FORMATTER);
            return ChronoUnit.DAYS.between(d1, d2);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Форматирует относительное время (например, "2 days ago", "in 5 days").
     *
     * @param date дата для форматирования
     * @return строка с относительным временем
     */
    public static String formatRelativeTime(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "Invalid date";
        }
        
        try {
            LocalDateTime targetDateTime;
            LocalDateTime now = LocalDateTime.now();
            
            if (date.contains(" ")) {
                targetDateTime = LocalDateTime.parse(date.trim(), DATETIME_FORMATTER);
            } else {
                targetDateTime = LocalDate.parse(date.trim(), DATE_FORMATTER).atStartOfDay();
            }
            
            long days = ChronoUnit.DAYS.between(now, targetDateTime);
            long hours = ChronoUnit.HOURS.between(now, targetDateTime);
            long minutes = ChronoUnit.MINUTES.between(now, targetDateTime);
            
            if (days > 0) {
                if (days > 365) {
                    long years = days / 365;
                    return years + " year" + (years > 1 ? "s" : "") + " from now";
                } else if (days > 30) {
                    long months = days / 30;
                    return months + " month" + (months > 1 ? "s" : "") + " from now";
                }
                return days + " day" + (days > 1 ? "s" : "") + " from now";
            } else if (days < 0) {
                days = Math.abs(days);
                if (days > 365) {
                    long years = days / 365;
                    return years + " year" + (years > 1 ? "s" : "") + " ago";
                } else if (days > 30) {
                    long months = days / 30;
                    return months + " month" + (months > 1 ? "s" : "") + " ago";
                }
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else {
                // В пределах одного дня
                if (hours > 0) {
                    return Math.abs(hours) + " hour" + (Math.abs(hours) > 1 ? "s" : "") + 
                           (hours > 0 ? " from now" : " ago");
                } else if (minutes > 0) {
                    return Math.abs(minutes) + " minute" + (Math.abs(minutes) > 1 ? "s" : "") +
                           (minutes > 0 ? " from now" : " ago");
                } else {
                    return "just now";
                }
            }
        } catch (DateTimeParseException e) {
            return "Invalid date format";
        }
    }

    /**
     * Проверяет, истекла ли дата (сравнивает с текущей датой).
     *
     * @param date дата для проверки
     * @return true, если дата в прошлом
     */
    public static boolean isExpired(String date) {
        if (date == null || date.trim().isEmpty()) {
            return true;
        }
        
        try {
            if (date.contains(" ")) {
                LocalDateTime dateTime = LocalDateTime.parse(date.trim(), DATETIME_FORMATTER);
                return LocalDateTime.now().isAfter(dateTime);
            } else {
                LocalDate localDate = LocalDate.parse(date.trim(), DATE_FORMATTER);
                return LocalDate.now().isAfter(localDate);
            }
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    /**
     * Проверяет, активна ли дата (не в прошлом).
     *
     * @param date дата для проверки
     * @return true, если дата в будущем или настоящем
     */
    public static boolean isActive(String date) {
        return !isExpired(date);
    }

    /**
     * Форматирует дату в другой формат.
     *
     * @param date исходная дата
     * @param format целевой формат
     * @return отформатированная дата
     */
    public static String formatDate(String date, String format) {
        if (date == null || date.trim().isEmpty()) {
            return "";
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            if (date.contains(" ")) {
                LocalDateTime dateTime = LocalDateTime.parse(date.trim(), DATETIME_FORMATTER);
                return dateTime.format(formatter);
            } else {
                LocalDate localDate = LocalDate.parse(date.trim(), DATE_FORMATTER);
                return localDate.format(formatter);
            }
        } catch (DateTimeParseException e) {
            return "Invalid date";
        }
    }

    /**
     * Нормализует дату, приводя к полному формату с временем.
     *
     * @param date дата для нормализации
     * @return дата в формате yyyy-MM-dd HH:mm:ss
     */
    private static String normalizeDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        String trimmed = date.trim();
        if (trimmed.contains(" ")) {
            return trimmed;
        }
        return trimmed + " 00:00:00";
    }

    /**
     * Получает дату в формате YYYY-MM-DD из даты с временем.
     *
     * @param dateTime дата с временем
     * @return только дата
     */
    public static String extractDate(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return "";
        }
        
        if (dateTime.contains(" ")) {
            return dateTime.split(" ")[0];
        }
        return dateTime.trim();
    }

    /**
     * Получает время в формате HH:MM:SS из даты с временем.
     *
     * @param dateTime дата с временем
     * @return только время
     */
    public static String extractTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return "";
        }
        
        if (dateTime.contains(" ")) {
            String[] parts = dateTime.split(" ");
            return parts.length > 1 ? parts[1] : "00:00:00";
        }
        return "00:00:00";
    }
}
