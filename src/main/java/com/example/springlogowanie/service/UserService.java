package com.example.springlogowanie.service;

import com.example.springlogowanie.model.Cart;
import com.example.springlogowanie.model.Role;
import com.example.springlogowanie.model.User;
import com.example.springlogowanie.repository.RoleRepository;
import com.example.springlogowanie.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public String registerUser(User user) {
        System.out.println("Registering new user: " + user.getUsername());

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            System.err.println("Registration failed: Username already exists");
            return "failure";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("Password encoded successfully for user: " + user.getUsername());

        user.setCart(new Cart());
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_USER");
            roleRepository.save(newRole);
            System.out.println("Created and saved new role: ROLE_USER");
            return newRole;
        });

        user.getRoles().add(userRole);
        userRepository.save(user);

        System.out.println("User registered successfully: " + user.getUsername());
        return "success";
    }

    @Transactional
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Fetching current authenticated user");

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            System.out.println("Authenticated username: " + username);
            return userRepository.findByUsername(username).orElseThrow(() -> {
                System.err.println("Current user not found in database: " + username);
                return new RuntimeException("User not found");
            });
        }

        System.err.println("No authenticated user found in SecurityContext");
        throw new RuntimeException("No authenticated user found");
    }
    @Transactional
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from database");
        return userRepository.findAll();
    }
}
