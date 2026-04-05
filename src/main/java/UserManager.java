import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Потокобезопасный менеджер пользователей для управления пользователями в системе RBAC.
 * Реализует интерфейс Repository<User> и предоставляет дополнительные методы
 * для поиска, фильтрации и сортировки пользователей.
 */
public class UserManager implements Repository<User> {
    private final Map<String, User> users; // ключ — username (ConcurrentHashMap)

    /**
     * Создает новый UserManager с пустым хранилищем.
     */
    public UserManager() {
        this.users = new ConcurrentHashMap<>();
    }

    /**
     * Добавляет пользователя в репозиторий.
     *
     * @param user пользователь для добавления
     * @throws IllegalArgumentException если пользователь с таким username уже существует
     * @throws IllegalArgumentException если пользователь null или не прошёл валидацию
     */
    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("User with username '" + user.username() + "' already exists");
        }
        users.put(user.username(), user);
    }

    /**
     * Удаляет пользователя из репозитория.
     *
     * @param user пользователь для удаления
     * @return true, если пользователь был удалён, false иначе
     */
    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }
        return users.remove(user.username()) != null;
    }

    /**
     * Находит пользователя по имени пользователя (username).
     *
     * @param username имя пользователя для поиска
     * @return Optional, содержащий пользователя, если найден
     */
    @Override
    public Optional<User> findById(String username) {
        return Optional.ofNullable(users.get(username));
    }

    /**
     * Возвращает всех пользователей из репозитория.
     *
     * @return список всех пользователей
     */
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /**
     * Возвращает количество пользователей в репозитории.
     *
     * @return количество пользователей
     */
    @Override
    public int count() {
        return users.size();
    }

    /**
     * Очищает репозиторий, удаляя всех пользователей.
     */
    @Override
    public void clear() {
        users.clear();
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return Optional, содержащий пользователя, если найден
     */
    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    /**
     * Проверяет, существует ли пользователь с указанным именем.
     *
     * @param username имя пользователя для проверки
     * @return true, если пользователь существует, false иначе
     */
    public boolean exists(String username) {
        return users.containsKey(username);
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param username имя пользователя для обновления
     * @param newFullName новое полное имя
     * @param newEmail новый email
     * @throws IllegalArgumentException если пользователь не найден
     * @throws IllegalArgumentException если новые данные не прошли валидацию
     */
    public synchronized void update(String username, String newFullName, String newEmail) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("User with username '" + username + "' not found");
        }

        // Валидация новых данных
        User.validate(username, newFullName, newEmail);

        // Создаем нового пользователя с обновленными данными
        User updatedUser = new User(username, newFullName, newEmail);
        users.put(username, updatedUser);
    }

    /**
     * Находит пользователя по email (потокобезопасная версия).
     *
     * @param email email для поиска
     * @return Optional, содержащий пользователя, если найден
     */
    public synchronized Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return users.values().stream()
                .filter(user -> email.equals(user.email()))
                .findFirst();
    }

    /**
     * Находит пользователей, соответствующих указанному фильтру (потокобезопасная версия).
     *
     * @param filter фильтр для поиска
     * @return список пользователей, соответствующих фильтру
     */
    public synchronized List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    /**
     * Находит пользователей, соответствующих указанному фильтру, используя parallelStream.
     *
     * @param filter фильтр для поиска
     * @return список пользователей, соответствующих фильтру
     */
    public List<User> findByFilterParallel(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return users.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    /**
     * Находит всех пользователей с применением фильтра и сортировки (потокобезопасная версия).
     *
     * @param filter фильтр для поиска (может быть null)
     * @param sorter компаратор для сортировки (может быть null)
     * @return отфильтрованный и отсортированный список пользователей
     */
    public synchronized List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    /**
     * Переопределение equals для сравнения менеджеров по содержимому.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserManager that = (UserManager) obj;
        return users.equals(that.users);
    }

    /**
     * Переопределение hashCode для согласованности с equals.
     */
    @Override
    public int hashCode() {
        return users.hashCode();
    }
}
