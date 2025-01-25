package com.example.springlogowanie.repository;

import com.example.springlogowanie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Wyszukuje użytkownika na podstawie nazwy użytkownika.
     *
     * @param username nazwa użytkownika
     * @return opcjonalny użytkownik
     */
    Optional<User> findByUsername(String username);
}
