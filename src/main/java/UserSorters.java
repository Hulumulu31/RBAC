import java.util.Comparator;

/**
 * Класс с фабричными методами для создания компараторов пользователей.
 */
public class UserSorters {

    private UserSorters() {
        // Утилитный класс, не должен быть инстанцирован
    }

    /**
     * Создает компаратор для сортировки пользователей по имени пользователя.
     *
     * @return компаратор Comparator<User>
     */
    public static Comparator<User> byUsername() {
        return Comparator.comparing(User::username);
    }

    /**
     * Создает компаратор для сортировки пользователей по полному имени.
     *
     * @return компаратор Comparator<User>
     */
    public static Comparator<User> byFullName() {
        return Comparator.comparing(User::fullName);
    }

    /**
     * Создает компаратор для сортировки пользователей по email.
     *
     * @return компаратор Comparator<User>
     */
    public static Comparator<User> byEmail() {
        return Comparator.comparing(User::email);
    }
}
