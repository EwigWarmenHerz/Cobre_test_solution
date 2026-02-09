package co.com.bancolombia.model.user;

public record User(
        int id,
        String name,
        String surname,
        String email
) {
    public User {
        if (name == null || name.isBlank()) {

            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Surname cannot be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        name = name.toUpperCase();
        surname = surname.toUpperCase();
        email = email.toUpperCase();
    }
}
