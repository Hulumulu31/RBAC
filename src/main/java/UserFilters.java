import java.util.Objects;

/**
 * Класс с фабричными методами для создания фильтров пользователей.
 */
public class UserFilters {

    private UserFilters() {
        // Утилитный класс, не должен быть инстанцирован
    }

    /**
     * Создает фильтр для точного совпадения имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return фильтр UserFilter
     */
    public static UserFilter byUsername(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return user -> username.equals(user.username());
    }

    /**
     * Создает фильтр для поиска по подстроке в имени пользователя (игнорируя регистр).
     *
     * @param substring подстрока для поиска
     * @return фильтр UserFilter
     */
    public static UserFilter byUsernameContains(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        String lowerSubstring = substring.toLowerCase();
        return user -> user.username().toLowerCase().contains(lowerSubstring);
    }

    /**
     * Создает фильтр для точного совпадения email.
     *
     * @param email email для поиска
     * @return фильтр UserFilter
     */
    public static UserFilter byEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        return user -> email.equals(user.email());
    }

    /**
     * Создает фильтр для поиска по домену email (заканчивается на домен).
     *
     * @param domain домен для поиска (например, "@company.com")
     * @return фильтр UserFilter
     */
    public static UserFilter byEmailDomain(String domain) {
        Objects.requireNonNull(domain, "Domain cannot be null");
        String lowerDomain = domain.toLowerCase();
        return user -> user.email().toLowerCase().endsWith(lowerDomain);
    }

    /**
     * Создает фильтр для поиска по подстроке в полном имени (игнорируя регистр).
     *
     * @param substring подстрока для поиска
     * @return фильтр UserFilter
     */
    public static UserFilter byFullNameContains(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        String lowerSubstring = substring.toLowerCase();
        return user -> user.fullName().toLowerCase().contains(lowerSubstring);
    }
}
