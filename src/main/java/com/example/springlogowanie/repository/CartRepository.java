package com.example.springlogowanie.repository;

import com.example.springlogowanie.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Integer> {
}
