/**
 * Функциональный интерфейс для фильтрации пользователей.
 */
@FunctionalInterface
interface UserFilter {
    boolean test(User user);

    /**
     * Комбинирует текущий фильтр с другим с помощью логического И.
     * Оба условия должны выполняться.
     */
    default UserFilter and(UserFilter other) {
        if (other == null) {
            return this;
        }
        return user -> this.test(user) && other.test(user);
    }

    /**
     * Комбинирует текущий фильтр с другим с помощью логического ИЛИ.
     * Хотя бы одно условие должно выполняться.
     */
    default UserFilter or(UserFilter other) {
        if (other == null) {
            return this;
        }
        return user -> this.test(user) || other.test(user);
    }
}
