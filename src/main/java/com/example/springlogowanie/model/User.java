package com.example.springlogowanie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;


import java.util.*;

@Entity
@Table(name = "users")
public class User {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @OneToOne (cascade = CascadeType. ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @NotBlank(message = "Nazwa uzytkownika jest wymagana")
    private String username;
    @NotBlank(message = "Haslo jest wymagane")
    private String password;

    @Setter
    @Getter
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

    @Getter
    @Setter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
}