import java.util.Objects;

public record Permission(String name, String resource, String description) {
    
    // Пользовательский канонический конструктор для валидации и нормализации полей
    public Permission {
        // Преобразование name в верхний регистр
        name = Objects.requireNonNull(name, "Name cannot be null").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        name = name.toUpperCase();
        
        // Проверка, что name не содержит пробелов
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Name cannot contain spaces");
        }
        
        // Преобразование resource в нижний регистр
        resource = Objects.requireNonNull(resource, "Resource cannot be null").trim();
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Resource cannot be empty");
        }
        resource = resource.toLowerCase();
        
        // Проверка, что description не пустой
        description = Objects.requireNonNull(description, "Description cannot be null").trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }
    
    // Метод для форматированного вывода
    public String format() {
        return name + " on " + resource + ": " + description;
    }

    // Метод для поиска по шаблонам
    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = namePattern == null || namePattern.isEmpty() ||
                             name.contains(namePattern) || name.equals(namePattern);
        boolean resourceMatches = resourcePattern == null || resourcePattern.isEmpty() ||
                                 resource.contains(resourcePattern) || resource.equals(resourcePattern);
        return nameMatches && resourceMatches;
    }
}