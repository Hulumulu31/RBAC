/**
 * Функциональный интерфейс для фильтрации назначений ролей.
 */
@FunctionalInterface
interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    /**
     * Комбинирует текущий фильтр с другим с помощью логического И.
     * Оба условия должны выполняться.
     */
    default AssignmentFilter and(AssignmentFilter other) {
        if (other == null) {
            return this;
        }
        return assignment -> this.test(assignment) && other.test(assignment);
    }

    /**
     * Комбинирует текущий фильтр с другим с помощью логического ИЛИ.
     * Хотя бы одно условие должно выполняться.
     */
    default AssignmentFilter or(AssignmentFilter other) {
        if (other == null) {
            return this;
        }
        return assignment -> this.test(assignment) || other.test(assignment);
    }
}
