public record User(String username, String fullName, String email) {

    // Статический метод для валидации и создания User
    public static User validate(String username, String fullName, String email) {
        // Проверка, что все поля обязательны (не null и не пустые строки)
        ValidationUtils.requireNonEmpty(username, "Username");
        ValidationUtils.requireNonEmpty(fullName, "Full name");
        ValidationUtils.requireNonEmpty(email, "Email");

        // Проверка формата username
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be 3-20 characters, start with a letter, and contain only letters, digits, and underscores");
        }

        // Проверка формата email
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return new User(username, fullName, email);
    }
    
    // Конструктор record автоматически проверяет параметры через статический метод
    // Убрали автоматический вызов validate из конструктора record, 
    // чтобы избежать рекурсии. Валидация теперь происходит только в статическом методе.
    
    // Метод для форматированного вывода
    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }
}