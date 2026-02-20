import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Класс с фабричными методами для создания фильтров назначений.
 */
public class AssignmentFilters {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AssignmentFilters() {
        // Утилитный класс, не должен быть инстанцирован
    }

    /**
     * Создает фильтр для назначений для конкретного пользователя.
     *
     * @param user пользователь для поиска
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter byUser(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        return assignment -> assignment.user().equals(user);
    }

    /**
     * Создает фильтр для назначений для пользователя с указанным именем.
     *
     * @param username имя пользователя для поиска
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter byUsername(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return assignment -> assignment.user().username().equals(username);
    }

    /**
     * Создает фильтр для назначений конкретной роли.
     *
     * @param role роль для поиска
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter byRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        return assignment -> assignment.role().equals(role);
    }

    /**
     * Создает фильтр для назначений роли с указанным именем.
     *
     * @param roleName имя роли для поиска
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter byRoleName(String roleName) {
        Objects.requireNonNull(roleName, "Role name cannot be null");
        return assignment -> assignment.role().getName().equals(roleName);
    }

    /**
     * Создает фильтр для только активных назначений.
     *
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    /**
     * Создает фильтр для только неактивных назначений.
     *
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    /**
     * Создает фильтр для назначений указанного типа.
     *
     * @param type тип назначения: "PERMANENT" или "TEMPORARY"
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter byType(String type) {
        Objects.requireNonNull(type, "Type cannot be null");
        String upperType = type.toUpperCase();
        return assignment -> assignment.assignmentType().equals(upperType);
    }

    /**
     * Создает фильтр для назначений, сделанных указанным пользователем.
     *
     * @param username имя пользователя, который назначил
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter assignedBy(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    /**
     * Создает фильтр для назначений, сделанных после указанной даты.
     *
     * @param date дата в формате "yyyy-MM-dd HH:mm:ss"
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter assignedAfter(String date) {
        Objects.requireNonNull(date, "Date cannot be null");
        LocalDateTime assignedDate = LocalDateTime.parse(date, DATE_FORMATTER);
        return assignment -> {
            LocalDateTime assignmentDate = LocalDateTime.parse(
                assignment.metadata().assignedAt(), DATE_FORMATTER);
            return assignmentDate.isAfter(assignedDate) || assignmentDate.isEqual(assignedDate);
        };
    }

    /**
     * Создает фильтр для временных назначений, истекающих до указанной даты.
     * Работает только с TemporaryAssignment.
     *
     * @param date дата в формате "yyyy-MM-dd HH:mm:ss"
     * @return фильтр AssignmentFilter
     */
    public static AssignmentFilter expiringBefore(String date) {
        Objects.requireNonNull(date, "Date cannot be null");
        LocalDateTime expirationDate = LocalDateTime.parse(date, DATE_FORMATTER);
        return assignment -> {
            if (assignment instanceof TemporaryAssignment) {
                TemporaryAssignment tempAssignment = (TemporaryAssignment) assignment;
                LocalDateTime expires = LocalDateTime.parse(
                    tempAssignment.getExpiresAt(), DATE_FORMATTER);
                return expires.isBefore(expirationDate) || expires.isEqual(expirationDate);
            }
            return false;
        };
    }
}
