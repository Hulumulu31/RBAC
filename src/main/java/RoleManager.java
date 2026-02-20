import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер ролей для управления ролями в системе RBAC.
 * Реализует интерфейс Repository<Role> и предоставляет дополнительные методы
 * для поиска, фильтрации, сортировки и управления правами ролей.
 */
public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById;      // ключ — id роли
    private final Map<String, Role> rolesByName;    // ключ — имя роли

    /**
     * Создает новый RoleManager с пустым хранилищем.
     */
    public RoleManager() {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
    }

    /**
     * Добавляет роль в репозиторий.
     *
     * @param role роль для добавления
     * @throws IllegalArgumentException если роль с таким именем уже существует
     * @throws IllegalArgumentException если роль null
     */
    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
        }
        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }

    /**
     * Удаляет роль из репозитория.
     *
     * @param role роль для удаления
     * @return true, если роль была удалена, false иначе
     */
    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }
        Role removedById = rolesById.remove(role.getId());
        if (removedById != null) {
            rolesByName.remove(role.getName());
            return true;
        }
        return false;
    }

    /**
     * Находит роль по идентификатору.
     *
     * @param id идентификатор роли
     * @return Optional, содержащий роль, если найдена
     */
    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    /**
     * Возвращает все роли из репозитория.
     *
     * @return список всех ролей
     */
    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    /**
     * Возвращает количество ролей в репозитории.
     *
     * @return количество ролей
     */
    @Override
    public int count() {
        return rolesById.size();
    }

    /**
     * Очищает репозиторий, удаляя все роли.
     */
    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    /**
     * Находит роль по имени.
     *
     * @param name имя роли для поиска
     * @return Optional, содержащий роль, если найдена
     */
    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    /**
     * Находит роли, соответствующие указанному фильтру.
     *
     * @param filter фильтр для поиска
     * @return список ролей, соответствующих фильтру
     */
    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    /**
     * Находит все роли с применением фильтра и сортировки.
     *
     * @param filter фильтр для поиска (может быть null)
     * @param sorter компаратор для сортировки (может быть null)
     * @return отфильтрованный и отсортированный список ролей
     */
    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    /**
     * Проверяет, существует ли роль с указанным именем.
     *
     * @param name имя роли для проверки
     * @return true, если роль существует, false иначе
     */
    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    /**
     * Добавляет право доступа к роли.
     *
     * @param roleName имя роли
     * @param permission право для добавления
     * @throws IllegalArgumentException если роль не найдена
     * @throws IllegalArgumentException если permission null
     */
    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        role.addPermission(permission);
    }

    /**
     * Удаляет право доступа из роли.
     *
     * @param roleName имя роли
     * @param permission право для удаления
     * @throws IllegalArgumentException если роль не найдена
     */
    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }
        role.removePermission(permission);
    }

    /**
     * Находит роли, имеющие указанное право доступа.
     *
     * @param permissionName имя права
     * @param resource ресурс
     * @return список ролей, имеющих указанное право
     */
    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    /**
     * Переопределение equals для сравнения менеджеров по содержимому.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoleManager that = (RoleManager) obj;
        return rolesById.equals(that.rolesById) && rolesByName.equals(that.rolesByName);
    }

    /**
     * Переопределение hashCode для согласованности с equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(rolesById, rolesByName);
    }
}
