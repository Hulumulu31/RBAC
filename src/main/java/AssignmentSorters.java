import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Класс с фабричными методами для создания компараторов назначений.
 */
public class AssignmentSorters {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AssignmentSorters() {
        // Утилитный класс, не должен быть инстанцирован
    }

    /**
     * Создает компаратор для сортировки назначений по имени пользователя.
     *
     * @return компаратор Comparator<RoleAssignment>
     */
    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(assignment -> assignment.user().username());
    }

    /**
     * Создает компаратор для сортировки назначений по имени роли.
     *
     * @return компаратор Comparator<RoleAssignment>
     */
    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(assignment -> assignment.role().getName());
    }

    /**
     * Создает компаратор для сортировки назначений по дате назначения.
     *
     * @return компаратор Comparator<RoleAssignment>
     */
    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(assignment ->
            LocalDateTime.parse(assignment.metadata().assignedAt(), DATE_FORMATTER));
    }
}
