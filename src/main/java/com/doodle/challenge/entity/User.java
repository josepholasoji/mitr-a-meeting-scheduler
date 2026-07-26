package com.doodle.challenge.entity;

import com.doodle.challenge.event.UserCreated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends BaseEntity<User> {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    protected User() {
        // required by JPA
    }

    private User(UUID id, String name, String email, String passwordHash) {
        super(id);
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static User register(String name, String email, String passwordHash) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        User user = new User(UUID.randomUUID(), name, email, passwordHash);
        user.registerEvent(new UserCreated(user.getId()));
        return user;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
