import java.util.List;
import java.util.Scanner;

/**
 * Утилитный класс для интерактивного взаимодействия с пользователем.
 * Предоставляет методы для пошаговых диалогов и ввода данных.
 */
public class ConsoleUtils {

    /**
     * Запрашивает строку у пользователя.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param required обязательно ли значение (не пустое)
     * @return введённая строка
     */
    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty() && !required) {
                return "";
            }
            
            if (input.isEmpty() && required) {
                System.out.println(FormatUtils.ANSI_RED + "This field is required. Please enter a value." + FormatUtils.ANSI_RESET);
                continue;
            }
            
            return input;
        }
    }

    /**
     * Запрашивает строку у пользователя с валидацией.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param required обязательно ли значение
     * @param validator функция валидации строки
     * @param errorMessage сообщение об ошибке валидации
     * @return введённая строка, прошедшая валидацию
     */
    public static String promptString(Scanner scanner, String message, boolean required, 
                                      java.util.function.Predicate<String> validator, 
                                      String errorMessage) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty() && !required) {
                return "";
            }
            
            if (input.isEmpty() && required) {
                System.out.println(FormatUtils.ANSI_RED + "This field is required. Please enter a value." + FormatUtils.ANSI_RESET);
                continue;
            }
            
            if (validator != null && !validator.test(input)) {
                System.out.println(FormatUtils.ANSI_RED + errorMessage + FormatUtils.ANSI_RESET);
                continue;
            }
            
            return input;
        }
    }

    /**
     * Запрашивает целое число в указанном диапазоне.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @return введённое число
     */
    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            
            try {
                int value = Integer.parseInt(input);
                
                if (value < min || value > max) {
                    System.out.println(FormatUtils.ANSI_RED + 
                        "Please enter a number between " + min + " and " + max + "." + 
                        FormatUtils.ANSI_RESET);
                    continue;
                }
                
                return value;
            } catch (NumberFormatException e) {
                System.out.println(FormatUtils.ANSI_RED + 
                    "Invalid number. Please enter a valid integer." + 
                    FormatUtils.ANSI_RESET);
            }
        }
    }

    /**
     * Запрашивает целое число (без ограничений диапазона).
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @return введённое число
     */
    public static int promptInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(FormatUtils.ANSI_RED + 
                    "Invalid number. Please enter a valid integer." + 
                    FormatUtils.ANSI_RESET);
            }
        }
    }

    /**
     * Запрашивает подтверждение (yes/no).
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @return true если пользователь ввёл "yes" (или "y"), false иначе
     */
    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("yes") || input.equals("y")) {
                return true;
            } else if (input.equals("no") || input.equals("n")) {
                return false;
            } else {
                System.out.println(FormatUtils.ANSI_YELLOW + 
                    "Please enter 'yes' or 'no'." + 
                    FormatUtils.ANSI_RESET);
            }
        }
    }

    /**
     * Запрашивает выбор из списка вариантов.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param options список вариантов для выбора
     * @param <T> тип элементов списка
     * @return выбранный элемент
     */
    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be null or empty");
        }

        System.out.println(message);
        
        // Выводим варианты с номерами
        for (int i = 0; i < options.size(); i++) {
            System.out.println(FormatUtils.ANSI_CYAN + "  [" + (i + 1) + "] " + options.get(i) + FormatUtils.ANSI_RESET);
        }
        
        while (true) {
            System.out.print("Enter your choice (1-" + options.size() + "): ");
            String input = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(input);
                
                if (choice < 1 || choice > options.size()) {
                    System.out.println(FormatUtils.ANSI_RED + 
                        "Please enter a number between 1 and " + options.size() + "." + 
                        FormatUtils.ANSI_RESET);
                    continue;
                }
                
                return options.get(choice - 1);
            } catch (NumberFormatException e) {
                System.out.println(FormatUtils.ANSI_RED + 
                    "Invalid number. Please enter a valid integer." + 
                    FormatUtils.ANSI_RESET);
            }
        }
    }

    /**
     * Запрашивает выбор из списка вариантов с кастомным отображением.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param options список вариантов для выбора
     * @param displayFunction функция для отображения элемента
     * @param <T> тип элементов списка
     * @return выбранный элемент
     */
    public static <T> T promptChoice(Scanner scanner, String message, List<T> options, 
                                     java.util.function.Function<T, String> displayFunction) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be null or empty");
        }

        System.out.println(message);
        
        // Выводим варианты с номерами
        for (int i = 0; i < options.size(); i++) {
            String display = displayFunction != null ? 
                displayFunction.apply(options.get(i)) : options.get(i).toString();
            System.out.println(FormatUtils.ANSI_CYAN + "  [" + (i + 1) + "] " + display + FormatUtils.ANSI_RESET);
        }
        
        while (true) {
            System.out.print("Enter your choice (1-" + options.size() + "): ");
            String input = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(input);
                
                if (choice < 1 || choice > options.size()) {
                    System.out.println(FormatUtils.ANSI_RED + 
                        "Please enter a number between 1 and " + options.size() + "." + 
                        FormatUtils.ANSI_RESET);
                    continue;
                }
                
                return options.get(choice - 1);
            } catch (NumberFormatException e) {
                System.out.println(FormatUtils.ANSI_RED + 
                    "Invalid number. Please enter a valid integer." + 
                    FormatUtils.ANSI_RESET);
            }
        }
    }

    /**
     * Запрашивает дату в формате YYYY-MM-DD.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param required обязательно ли значение
     * @return введённая дата
     */
    public static String promptDate(Scanner scanner, String message, boolean required) {
        return promptString(scanner, message, required, 
            ValidationUtils::isValidDate,
            "Invalid date format. Please use YYYY-MM-DD or YYYY-MM-DD HH:MM:SS");
    }

    /**
     * Запрашивает email у пользователя.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param required обязательно ли значение
     * @return введённый email
     */
    public static String promptEmail(Scanner scanner, String message, boolean required) {
        return promptString(scanner, message, required, 
            ValidationUtils::isValidEmail,
            "Invalid email format. Please enter a valid email address");
    }

    /**
     * Запрашивает имя пользователя у пользователя.
     *
     * @param scanner сканер для чтения ввода
     * @param message сообщение-подсказка
     * @param required обязательно ли значение
     * @return введённое имя пользователя
     */
    public static String promptUsername(Scanner scanner, String message, boolean required) {
        return promptString(scanner, message, required, 
            ValidationUtils::isValidUsername,
            "Invalid username. Must be 3-20 characters, start with a letter, and contain only letters, digits, and underscores");
    }

    /**
     * Выводит сообщение об успехе.
     *
     * @param message сообщение
     */
    public static void printSuccess(String message) {
        System.out.println(FormatUtils.ANSI_GREEN + "✓ " + message + FormatUtils.ANSI_RESET);
    }

    /**
     * Выводит сообщение об ошибке.
     *
     * @param message сообщение
     */
    public static void printError(String message) {
        System.out.println(FormatUtils.ANSI_RED + "✗ " + message + FormatUtils.ANSI_RESET);
    }

    /**
     * Выводит предупреждение.
     *
     * @param message сообщение
     */
    public static void printWarning(String message) {
        System.out.println(FormatUtils.ANSI_YELLOW + "⚠ " + message + FormatUtils.ANSI_RESET);
    }

    /**
     * Выводит информационное сообщение.
     *
     * @param message сообщение
     */
    public static void printInfo(String message) {
        System.out.println(FormatUtils.ANSI_BLUE + "ℹ " + message + FormatUtils.ANSI_RESET);
    }

    /**
     * Очищает консоль (работает не во всех средах).
     */
    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Игнорируем ошибки очистки консоли
        }
    }

    /**
     * Делает паузу (ожидание нажатия Enter).
     *
     * @param message сообщение-подсказка
     */
    public static void pause(String message) {
        System.out.print(message);
        new Scanner(System.in).nextLine();
    }

    /**
     * Делает паузу с сообщением по умолчанию.
     */
    public static void pause() {
        pause("Press Enter to continue...");
    }
}
