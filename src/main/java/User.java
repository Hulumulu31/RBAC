public record User(String username, String fullName, String email) {
    
    // Статический метод для валидации и создания User
    public static User validate(String username, String fullName, String email) {
        // Проверка, что все поля обязательны (не null и не пустые строки)
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        // Проверка длины username (3-20 символов)
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Username must be between 3 and 20 characters");
        }
        
        // Проверка формата username (только латинские буквы, цифры и подчеркивание)
        for (char c : username.toCharArray()) {
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
                throw new IllegalArgumentException("Username can only contain letters, digits, and underscores");
            }
        }
        
        // Проверка формата email (содержит @ и точку после @)
        if (!email.contains("@") || !email.substring(email.indexOf('@')).contains(".")) {
            throw new IllegalArgumentException("Email must contain @ and a dot after @");
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