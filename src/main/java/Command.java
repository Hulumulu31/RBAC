import java.util.Scanner;

/**
 * Функциональный интерфейс для команд системы RBAC.
 */
@FunctionalInterface
public interface Command {
    /**
     * Выполняет команду.
     *
     * @param scanner сканер для чтения ввода пользователя
     * @param system система RBAC для доступа к менеджерам
     */
    void execute(Scanner scanner, RBACSystem system);
}
