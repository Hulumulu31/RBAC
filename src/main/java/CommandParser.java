import java.util.*;
import java.util.stream.Collectors;

/**
 * Парсер команд и меню системы RBAC.
 */
public class CommandParser {
    private final Map<String, Command> commands;
    private final Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new HashMap<>();
        this.commandDescriptions = new HashMap<>();
    }

    /**
     * Регистрирует команду.
     */
    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    /**
     * Выполняет команду по имени.
     */
    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName.toLowerCase());
        if (command != null) {
            command.execute(scanner, system);
        } else {
            System.out.println(FormatUtils.ANSI_RED + "Unknown command: " + commandName + 
                ". Type 'help' for available commands." + FormatUtils.ANSI_RESET);
        }
    }

    /**
     * Выводит справку по всем командам.
     */
    public void printHelp() {
        System.out.println("\n" + FormatUtils.formatHeader("Available Commands"));

        String[] headers = {"Command", "Description"};
        List<String[]> rows = new ArrayList<>();

        List<String> sortedCommands = commandDescriptions.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        for (String cmd : sortedCommands) {
            rows.add(new String[]{cmd, commandDescriptions.get(cmd)});
        }

        System.out.println(FormatUtils.formatTable(headers, rows));
    }

    /**
     * Парсит ввод и выполняет команду.
     */
    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(scanner, system);
        } else {
            System.out.println(FormatUtils.ANSI_RED + "Unknown command: " + commandName + 
                ". Type 'help' for available commands." + FormatUtils.ANSI_RESET);
        }
    }

    /**
     * Проверяет, существует ли команда.
     */
    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName.toLowerCase());
    }
}
