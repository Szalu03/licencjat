package com.example.springlogowanie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.antlr.v4.runtime.misc.NotNull;


import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa uzytkownika jest wymagana")
    private String username;
    @NotBlank(message = "Haslo jest wymagane")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    public @NotBlank(message = "Nazwa uzytkownika jest wymagana") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Nazwa uzytkownika jest wymagana") String username) {
        this.username = username;
    }

    public @NotBlank(message = "Haslo jest wymagane") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Haslo jest wymagane") String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}