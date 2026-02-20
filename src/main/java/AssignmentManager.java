import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер назначений для управления назначениями ролей в системе RBAC.
 * Реализует интерфейс Repository<RoleAssignment> и предоставляет дополнительные методы
 * для поиска, фильтрации, сортировки и управления правами пользователей.
 */
public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments;  // ключ — assignmentId
    private final UserManager userManager;
    private final RoleManager roleManager;

    /**
     * Создает новый AssignmentManager с пустым хранилищем.
     *
     * @param userManager менеджер пользователей для проверки существования
     * @param roleManager менеджер ролей для проверки существования
     */
    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignments = new HashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    /**
     * Добавляет назначение в репозиторий.
     *
     * @param assignment назначение для добавления
     * @throws IllegalArgumentException если назначение null
     * @throws IllegalArgumentException если пользователь или роль не существуют
     * @throws IllegalArgumentException если у пользователя уже есть эта роль (активное назначение)
     */
    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }

        // Проверяем существование пользователя и роли
        if (!userManager.exists(assignment.user().username())) {
            throw new IllegalArgumentException("User '" + assignment.user().username() + "' does not exist");
        }
        if (!roleManager.exists(assignment.role().getName())) {
            throw new IllegalArgumentException("Role '" + assignment.role().getName() + "' does not exist");
        }

        // Проверяем, нет ли уже активного назначения этой роли пользователю
        String assignmentKey = getAssignmentKey(assignment.user(), assignment.role());
        if (hasActiveAssignment(assignment.user(), assignment.role())) {
            throw new IllegalArgumentException(
                "User '" + assignment.user().username() + "' already has an active assignment for role '" +
                assignment.role().getName() + "'");
        }

        assignments.put(assignment.assignmentId(), assignment);
    }

    /**
     * Удаляет назначение из репозитория.
     *
     * @param assignment назначение для удаления
     * @return true, если назначение было удалено, false иначе
     */
    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        return assignments.remove(assignment.assignmentId()) != null;
    }

    /**
     * Находит назначение по идентификатору.
     *
     * @param id идентификатор назначения
     * @return Optional, содержащий назначение, если найдено
     */
    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    /**
     * Возвращает все назначения из репозитория.
     *
     * @return список всех назначений
     */
    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    /**
     * Возвращает количество назначений в репозитории.
     *
     * @return количество назначений
     */
    @Override
    public int count() {
        return assignments.size();
    }

    /**
     * Очищает репозиторий, удаляя все назначения.
     */
    @Override
    public void clear() {
        assignments.clear();
    }

    /**
     * Находит назначения для конкретного пользователя.
     *
     * @param user пользователь для поиска
     * @return список назначений для пользователя
     */
    public List<RoleAssignment> findByUser(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return assignments.values().stream()
                .filter(assignment -> assignment.user().equals(user))
                .collect(Collectors.toList());
    }

    /**
     * Находит назначения для конкретной роли.
     *
     * @param role роль для поиска
     * @return список назначений для роли
     */
    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return assignments.values().stream()
                .filter(assignment -> assignment.role().equals(role))
                .collect(Collectors.toList());
    }

    /**
     * Находит назначения, соответствующие указанному фильтру.
     *
     * @param filter фильтр для поиска
     * @return список назначений, соответствующих фильтру
     */
    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    /**
     * Находит все назначения с применением фильтра и сортировки.
     *
     * @param filter фильтр для поиска (может быть null)
     * @param sorter компаратор для сортировки (может быть null)
     * @return отфильтрованный и отсортированный список назначений
     */
    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    /**
     * Возвращает все активные назначения.
     *
     * @return список активных назначений
     */
    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает все истёкшие назначения.
     *
     * @return список истёкших назначений
     */
    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(assignment -> !assignment.isActive())
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, имеет ли пользователь указанную роль.
     *
     * @param user пользователь
     * @param role роль
     * @return true, если пользователь имеет роль (активное назначение)
     */
    public boolean userHasRole(User user, Role role) {
        return hasActiveAssignment(user, role);
    }

    /**
     * Проверяет, имеет ли пользователь указанное право доступа.
     *
     * @param user пользователь
     * @param permissionName имя права
     * @param resource ресурс
     * @return true, если пользователь имеет право через какую-либо из своих ролей
     */
    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> userPermissions = getUserPermissions(user);
        return userPermissions.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                               p.resource().equalsIgnoreCase(resource));
    }

    /**
     * Возвращает все права пользователя из всех его активных ролей.
     *
     * @param user пользователь
     * @return множество всех прав пользователя
     */
    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> allPermissions = new HashSet<>();
        List<RoleAssignment> userAssignments = findByUser(user);

        for (RoleAssignment assignment : userAssignments) {
            if (assignment.isActive()) {
                allPermissions.addAll(assignment.role().getPermissions());
            }
        }

        return allPermissions;
    }

    /**
     * Отозвать назначение по идентификатору.
     * Работает только с PermanentAssignment.
     *
     * @param assignmentId идентификатор назначения
     * @throws IllegalArgumentException если назначение не найдено
     * @throws IllegalArgumentException если назначение не является постоянным
     */
    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");
        }
        if (!(assignment instanceof PermanentAssignment)) {
            throw new IllegalArgumentException("Only permanent assignments can be revoked");
        }
        ((PermanentAssignment) assignment).revoke();
    }

    /**
     * Продлить временное назначение.
     *
     * @param assignmentId идентификатор назначения
     * @param newExpirationDate новая дата истечения в формате "yyyy-MM-dd HH:mm:ss"
     * @throws IllegalArgumentException если назначение не найдено
     * @throws IllegalArgumentException если назначение не является временным
     */
    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");
        }
        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Only temporary assignments can be extended");
        }
        ((TemporaryAssignment) assignment).extend(newExpirationDate);
    }

    /**
     * Создаёт уникальный ключ для проверки дублирования назначений.
     */
    private String getAssignmentKey(User user, Role role) {
        return user.username() + "::" + role.getId();
    }

    /**
     * Проверяет, есть ли у пользователя активное назначение для данной роли.
     */
    private boolean hasActiveAssignment(User user, Role role) {
        return assignments.values().stream()
                .filter(assignment -> assignment.user().equals(user) &&
                                     assignment.role().equals(role))
                .anyMatch(RoleAssignment::isActive);
    }

    /**
     * Переопределение equals для сравнения менеджеров по содержимому.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AssignmentManager that = (AssignmentManager) obj;
        return assignments.equals(that.assignments);
    }

    /**
     * Переопределение hashCode для согласованности с equals.
     */
    @Override
    public int hashCode() {
        return assignments.hashCode();
    }
}
