package com.example.springlogowanie.repository;

import com.example.springlogowanie.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDAO extends JpaRepository<Product, Integer> {

    // Wyszukuje produkty na podstawie fragmentu nazwy, ignorując wielkość liter
    List<Product> findByNameContainingIgnoreCase(String name);
}
