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

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;


    @Transactional
    public String registerUser (User user) {
        if
        (userRepository.findByUsername (user.getUsername()).isPresent()){
            return "failure";
        }
        user.setPassword (passwordEncoder.encode(user.getPassword()));
        user.setCart(new Cart());
        Role userRole =
                roleRepository.findByName("USER").orElse(null);
//System.out.println(userRole.getName());
        if (userRole != null) {
            user.getRoles().add(userRole);
        }
        else {
            Role role = new Role(); role.setName("USER");
            user.getRoles().add(role);
            roleRepository.save(role);
        }
        userRepository.save(user);
        return "success";
    }

    @Transactional
    public void save (User user) { userRepository.save(user);
    }
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Transactional
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername(); return userRepository.findByUsername (username).orElse(null);
    }
}