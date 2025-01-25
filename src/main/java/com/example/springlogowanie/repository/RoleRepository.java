package com.example.springlogowanie.repository;

import com.example.springlogowanie.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Wyszukuje rolę na podstawie jej nazwy.
     *
     * @param name nazwa roli
     * @return opcjonalna rola
     */
    Optional<Role> findByName(String name);
}
