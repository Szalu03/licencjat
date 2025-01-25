package com.example.springlogowanie.repository;

import com.example.springlogowanie.model.Order;
import com.example.springlogowanie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Wyszukuje zamówienia użytkownika.
     *
     * @param user użytkownik
     * @return lista zamówień użytkownika
     */
    List<Order> findByUser(User user);
}
