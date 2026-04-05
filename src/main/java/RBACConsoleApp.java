import java.util.*;

/**
 * Интерактивная консольная утилита для управления пользователями, ролями и правами доступа
 * с использованием модели RBAC (Role-Based Access Control).
 *
 * Версия 2.0: Реализована через систему команд (Command/CommandParser/RBACSystem).
 */
public class RBACConsoleApp {
    private RBACSystem system;
    private CommandParser commandParser;
    private Scanner scanner;

    public RBACConsoleApp() {
        this.system = new RBACSystem();
        this.commandParser = new CommandParser();
        this.scanner = new Scanner(System.in);

        // Инициализируем систему (создаем начальные данные)
        this.system.initialize();

        // Регистрируем все команды
        CommandRegistry.registerAllCommands(this.commandParser);

        // Shutdown hook — гарантия корректного завершения при любом выходе
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (system != null) {
                system.shutdown();
            }
        }, "rbac-shutdown-hook"));
    }

    public void start() {
        System.out.println(FormatUtils.formatHeader("RBAC Console Application"));
        System.out.println("Welcome to the Role-Based Access Control system!");
        System.out.println("Type 'help' for commands list.\n");

        boolean running = true;
        while (running) {
            printMainMenu();
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            // Проверяем команду выхода
            String firstWord = input.split("\\s+")[0].toLowerCase();
            if (firstWord.equals("exit") || firstWord.equals("quit")) {
                if (ConsoleUtils.promptYesNo(scanner, "Are you sure you want to exit?")) {
                    shutdownSystem();
                    System.out.println(FormatUtils.ANSI_CYAN + "Exiting RBAC Console Application... Goodbye!" + FormatUtils.ANSI_RESET);
                    running = false;
                    break;
                }
                continue;
            }

            // Выполняем команду через парсер
            commandParser.parseAndExecute(input, scanner, system);
        }

        shutdownSystem();
        scanner.close();
    }

    /**
     * Корректно завершает все фоновые задачи перед выходом.
     */
    private void shutdownSystem() {
        if (system != null) {
            system.shutdown();
        }
    }

    private void printMainMenu() {
        System.out.println("\n" + FormatUtils.ANSI_CYAN + "┌────────────────────────────────────────────────────────────┐" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET + "  RBAC System - Main Menu                           " + FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "├────────────────────────────────────────────────────────────┤" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET + "  Current user: " + FormatUtils.padRight(system.getCurrentUser(), 32) + "  " + FormatUtils.ANSI_CYAN + "│" + FormatUtils.ANSI_RESET);
        System.out.println(FormatUtils.ANSI_CYAN + "└────────────────────────────────────────────────────────────┘" + FormatUtils.ANSI_RESET);
        System.out.print("\nEnter command (or 'help'): ");
    }

    public static void main(String[] args) {
        RBACConsoleApp app = new RBACConsoleApp();
        app.start();
    }
}
