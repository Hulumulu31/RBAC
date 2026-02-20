import java.util.Comparator;

/**
 * Класс с фабричными методами для создания компараторов ролей.
 */
public class RoleSorters {

    private RoleSorters() {
        // Утилитный класс, не должен быть инстанцирован
    }

    /**
     * Создает компаратор для сортировки ролей по имени.
     *
     * @return компаратор Comparator<Role>
     */
    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }

    /**
     * Создает компаратор для сортировки ролей по количеству прав.
     *
     * @return компаратор Comparator<Role>
     */
    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(role -> role.getPermissions().size());
    }
}
