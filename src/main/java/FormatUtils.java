import java.util.Collections;
import java.util.List;

/**
 * Утилитный класс для форматирования вывода.
 * Предоставляет методы для создания красивых таблиц, рамок и заголовков.
 */
public class FormatUtils {

    // ANSI коды цветов для консольного вывода (опционально)
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";

    /**
     * Форматирует данные в виде ASCII-таблицы с рамками.
     *
     * @param headers заголовки столбцов
     * @param rows строки данных (каждая строка - массив значений)
     * @return строка с отформатированной таблицей
     */
    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "No data to display";
        }

        int columnCount = headers.length;
        int[] columnWidths = new int[columnCount];

        // Инициализируем ширины столбцов заголовками
        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = headers[i] != null ? headers[i].length() : 0;
        }

        // Определяем максимальную ширину для каждого столбца
        for (String[] row : rows) {
            for (int i = 0; i < columnCount; i++) {
                String cellValue = (i < row.length && row[i] != null) ? row[i] : "";
                columnWidths[i] = Math.max(columnWidths[i], cellValue.length());
            }
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя граница
        sb.append(createHorizontalLine(columnWidths)).append("\n");

        // Заголовки
        sb.append("|");
        for (int i = 0; i < columnCount; i++) {
            sb.append(" ").append(padRight(headers[i] != null ? headers[i] : "", columnWidths[i])).append(" |");
        }
        sb.append("\n");

        // Разделитель после заголовков
        sb.append(createHorizontalLine(columnWidths)).append("\n");

        // Строки данных
        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < columnCount; i++) {
                String cellValue = (i < row.length && row[i] != null) ? row[i] : "";
                sb.append(" ").append(padRight(cellValue, columnWidths[i])).append(" |");
            }
            sb.append("\n");
        }

        // Нижняя граница
        sb.append(createHorizontalLine(columnWidths));

        return sb.toString();
    }

    /**
     * Создаёт горизонтальную линию для таблицы.
     */
    private static String createHorizontalLine(int[] columnWidths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        return sb.toString();
    }

    /**
     * Обрамляет текст рамкой.
     *
     * @param text текст для обрамления
     * @return строка с рамкой вокруг текста
     */
    public static String formatBox(String text) {
        if (text == null || text.isEmpty()) {
            return "+--+\n|  |\n+--+";
        }

        String[] lines = text.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, line.length());
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя граница
        sb.append("+").append("-".repeat(maxWidth + 2)).append("+\n");

        // Строки текста
        for (String line : lines) {
            sb.append("| ").append(padRight(line, maxWidth)).append(" |\n");
        }

        // Нижняя граница
        sb.append("+").append("-".repeat(maxWidth + 2)).append("+");

        return sb.toString();
    }

    /**
     * Форматирует заголовок секции.
     *
     * @param text текст заголовка
     * @return строка с отформатированным заголовком
     */
    public static String formatHeader(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int width = text.length() + 4;
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("=".repeat(width)).append("\n");
        sb.append("  ").append(text.toUpperCase()).append("  \n");
        sb.append("=".repeat(width)).append("\n");

        return sb.toString();
    }

    /**
     * Форматирует подзаголовок секции.
     *
     * @param text текст подзаголовка
     * @return строка с отформатированным подзаголовком
     */
    public static String formatSubHeader(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ").append(text).append(" ---\n");
        return sb.toString();
    }

    /**
     * Обрезает строку до указанной длины, добавляя "..." если необходимо.
     *
     * @param text исходная строка
     * @param maxLength максимальная длина
     * @return обрезанная строка
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Дополняет строку пробелами справа до указанной длины.
     *
     * @param text исходная строка
     * @param length целевая длина
     * @return дополненная строка
     */
    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }

    /**
     * Дополняет строку пробелами слева до указанной длины.
     *
     * @param text исходная строка
     * @param length целевая длина
     * @return дополненная строка
     */
    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return " ".repeat(length - text.length()) + text;
    }

    /**
     * Дополняет строку пробелами по центру до указанной длины.
     *
     * @param text исходная строка
     * @param length целевая длина
     * @return дополненная строка
     */
    public static String padCenter(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        
        int totalPadding = length - text.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    /**
     * Создаёт разделительную линию.
     *
     * @param width ширина линии
     * @param character символ для линии
     * @return строка с разделителем
     */
    public static String createLine(int width, char character) {
        return String.valueOf(character).repeat(width);
    }

    /**
     * Создаёт разделительную линию с заголовком.
     *
     * @param text текст в центре
     * @param width общая ширина
     * @param character символ для линии
     * @return строка с разделителем
     */
    public static String createLineWithText(String text, int width, char character) {
        if (text == null || text.isEmpty()) {
            return createLine(width, character);
        }
        
        String line = " " + text + " ";
        int remainingWidth = width - line.length();
        int leftWidth = remainingWidth / 2;
        int rightWidth = remainingWidth - leftWidth;
        
        return character + String.valueOf(character).repeat(leftWidth - 1) + 
               line + 
               String.valueOf(character).repeat(rightWidth - 1) + character;
    }

    /**
     * Форматирует число с разделителями тысяч.
     *
     * @param number число для форматирования
     * @return отформатированная строка
     */
    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }

    /**
     * Форматирует статус с цветом (ANSI).
     *
     * @param status текст статуса
     * @param useColor использовать ли цвет
     * @return отформатированный статус
     */
    public static String formatStatus(String status, boolean useColor) {
        if (!useColor) {
            return status;
        }

        String upperStatus = status != null ? status.toUpperCase() : "";
        
        if (upperStatus.contains("ACTIVE") || upperStatus.contains("SUCCESS") || upperStatus.contains("ON")) {
            return ANSI_GREEN + status + ANSI_RESET;
        } else if (upperStatus.contains("INACTIVE") || upperStatus.contains("ERROR") || upperStatus.contains("OFF")) {
            return ANSI_RED + status + ANSI_RESET;
        } else if (upperStatus.contains("WARNING") || upperStatus.contains("PENDING")) {
            return ANSI_YELLOW + status + ANSI_RESET;
        } else if (upperStatus.contains("INFO")) {
            return ANSI_BLUE + status + ANSI_RESET;
        }
        
        return status;
    }
}
