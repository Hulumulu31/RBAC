import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для системы команд RBAC.
 */
class CommandRegistryTest {

    private RBACSystem system;
    private CommandParser parser;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
        parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);
    }

    @Test
    @DisplayName("Все команды зарегистрированы")
    void allCommandsRegistered() {
        // Проверяем основные команды
        assertTrue(parser.hasCommand("user-list"));
        assertTrue(parser.hasCommand("user-create"));
        assertTrue(parser.hasCommand("user-view"));
        assertTrue(parser.hasCommand("user-update"));
        assertTrue(parser.hasCommand("user-delete"));
        assertTrue(parser.hasCommand("user-search"));
        
        assertTrue(parser.hasCommand("role-list"));
        assertTrue(parser.hasCommand("role-create"));
        assertTrue(parser.hasCommand("role-view"));
        assertTrue(parser.hasCommand("role-update"));
        assertTrue(parser.hasCommand("role-delete"));
        assertTrue(parser.hasCommand("role-add-permission"));
        assertTrue(parser.hasCommand("role-remove-permission"));
        assertTrue(parser.hasCommand("role-search"));
        
        assertTrue(parser.hasCommand("assign-role"));
        assertTrue(parser.hasCommand("revoke-role"));
        assertTrue(parser.hasCommand("assignment-list"));
        assertTrue(parser.hasCommand("assignment-list-user"));
        assertTrue(parser.hasCommand("assignment-list-role"));
        assertTrue(parser.hasCommand("assignment-active"));
        assertTrue(parser.hasCommand("assignment-expired"));
        assertTrue(parser.hasCommand("assignment-extend"));
        assertTrue(parser.hasCommand("assignment-search"));
        
        assertTrue(parser.hasCommand("permissions-user"));
        assertTrue(parser.hasCommand("permissions-check"));
        
        assertTrue(parser.hasCommand("help"));
        assertTrue(parser.hasCommand("stats"));
        assertTrue(parser.hasCommand("clear"));
        assertTrue(parser.hasCommand("exit"));
        assertTrue(parser.hasCommand("save"));
        assertTrue(parser.hasCommand("load"));
    }

    @Test
    @DisplayName("RBACSystem инициализирует начальные данные")
    void systemInitialization() {
        // Проверяем, что роли созданы
        assertTrue(system.getRoleManager().exists("Admin"));
        assertTrue(system.getRoleManager().exists("Manager"));
        assertTrue(system.getRoleManager().exists("Viewer"));
        
        // Проверяем, что админ создан
        assertTrue(system.getUserManager().exists("admin"));
        
        // Проверяем, что роль назначена
        User admin = system.getUserManager().findByUsername("admin").orElse(null);
        assertNotNull(admin);
        
        long adminRoleCount = system.getAssignmentManager().findByUser(admin).stream()
            .filter(RoleAssignment::isActive)
            .filter(a -> a.role().getName().equals("Admin"))
            .count();
        assertTrue(adminRoleCount > 0);
    }

    @Test
    @DisplayName("generateStatistics возвращает корректную статистику")
    void generateStatistics() {
        String stats = system.generateStatistics();
        
        assertNotNull(stats);
        assertTrue(stats.contains("RBAC System Statistics"));
        assertTrue(stats.contains("Users:"));
        assertTrue(stats.contains("Roles:"));
        assertTrue(stats.contains("Assignments:"));
        assertTrue(stats.contains("Top 3 most assigned roles"));
    }

    @Test
    @DisplayName("CommandParser регистрирует и выполняет команды")
    void commandParserRegistration() {
        CommandParser testParser = new CommandParser();
        boolean[] executed = {false};
        
        testParser.registerCommand("test", "Test command", (scanner, sys) -> executed[0] = true);
        
        assertTrue(testParser.hasCommand("test"));
        assertFalse(testParser.hasCommand("nonexistent"));
        
        // Выполняем команду
        testParser.executeCommand("test", null, system);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("CommandParser.printHelp не выбрасывает исключений")
    void printHelpDoesNotThrow() {
        assertDoesNotThrow(() -> parser.printHelp());
    }

    @Test
    @DisplayName("Команда user-list работает корректно")
    void userListCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("user-list", null, system);
        });
    }

    @Test
    @DisplayName("Команда role-list работает корректно")
    void roleListCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("role-list", null, system);
        });
    }

    @Test
    @DisplayName("Команда stats работает корректно")
    void statsCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("stats", null, system);
        });
    }

    @Test
    @DisplayName("Команда assignment-list работает корректно")
    void assignmentListCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("assignment-list", null, system);
        });
    }
    
    @Test
    @DisplayName("Команда assignment-active работает корректно")
    void assignmentActiveCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("assignment-active", null, system);
        });
    }

    @Test
    @DisplayName("Команда clear работает корректно")
    void clearCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("clear", null, system);
        });
    }

    @Test
    @DisplayName("Команда save работает корректно")
    void saveCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("save", null, system);
        });
    }

    @Test
    @DisplayName("Команда load работает корректно")
    void loadCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("load", null, system);
        });
    }

    @Test
    @DisplayName("Неизвестная команда обрабатывается корректно")
    void unknownCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("nonexistent-command", null, system);
        });
    }

    @Test
    @DisplayName("Команды чувствительны к регистру")
    void commandsCaseInsensitive() {
        assertTrue(parser.hasCommand("USER-LIST"));
        assertTrue(parser.hasCommand("User-List"));
        assertTrue(parser.hasCommand("user-LIST"));
    }
}