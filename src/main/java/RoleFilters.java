import java.util.Objects;

/**
 * Класс с фабричными методами для создания фильтров ролей.
 */
public class RoleFilters {

    private RoleFilters() {
        // Утилитный класс, не должен быть инстанцирован
    }

    /**
     * Создает фильтр для точного совпадения имени роли.
     *
     * @param name имя роли для поиска
     * @return фильтр RoleFilter
     */
    public static RoleFilter byName(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return role -> name.equals(role.getName());
    }

    /**
     * Создает фильтр для поиска по подстроке в имени роли (игнорируя регистр).
     *
     * @param substring подстрока для поиска
     * @return фильтр RoleFilter
     */
    public static RoleFilter byNameContains(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        String lowerSubstring = substring.toLowerCase();
        return role -> role.getName().toLowerCase().contains(lowerSubstring);
    }

    /**
     * Создает фильтр для поиска ролей, имеющих указанное право.
     *
     * @param permission право для поиска
     * @return фильтр RoleFilter
     */
    public static RoleFilter hasPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission cannot be null");
        return role -> role.hasPermission(permission);
    }

    /**
     * Создает фильтр для поиска ролей, имеющих указанное право по имени и ресурсу.
     *
     * @param permissionName имя права
     * @param resource ресурс
     * @return фильтр RoleFilter
     */
    public static RoleFilter hasPermission(String permissionName, String resource) {
        Objects.requireNonNull(permissionName, "Permission name cannot be null");
        Objects.requireNonNull(resource, "Resource cannot be null");
        return role -> role.hasPermission(permissionName, resource);
    }

    /**
     * Создает фильтр для поиска ролей, имеющих минимум N прав.
     *
     * @param n минимальное количество прав
     * @return фильтр RoleFilter
     */
    public static RoleFilter hasAtLeastNPermissions(int n) {
        return role -> role.getPermissions().size() >= n;
    }
}
