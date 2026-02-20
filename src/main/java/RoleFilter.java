/**
 * Функциональный интерфейс для фильтрации ролей.
 */
@FunctionalInterface
interface RoleFilter {
    boolean test(Role role);

    /**
     * Комбинирует текущий фильтр с другим с помощью логического И.
     * Оба условия должны выполняться.
     */
    default RoleFilter and(RoleFilter other) {
        if (other == null) {
            return this;
        }
        return role -> this.test(role) && other.test(role);
    }

    /**
     * Комбинирует текущий фильтр с другим с помощью логического ИЛИ.
     * Хотя бы одно условие должно выполняться.
     */
    default RoleFilter or(RoleFilter other) {
        if (other == null) {
            return this;
        }
        return role -> this.test(role) || other.test(role);
    }
}
