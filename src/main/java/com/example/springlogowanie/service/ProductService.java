package com.example.springlogowanie.service;

import com.example.springlogowanie.model.Cart;
import com.example.springlogowanie.model.Order;
import com.example.springlogowanie.model.Product;
import com.example.springlogowanie.model.User;
import com.example.springlogowanie.repository.ProductDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private UserService userService;

    @Autowired
    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Transactional
    public void save(Product product) {
        productDAO.save(product);
    }

    @Transactional
    public List<Product> getAll() {
        return productDAO.findAll();
    }

    @Transactional
    public List<Product> searchProducts(String query) {
        return productDAO.findByNameContainingIgnoreCase(query); // Wywołaj metodę repozytorium
    }

    @Transactional
    public void delete(int id) {
        productDAO.deleteById(id);
    }
    public Cart getCart() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getCart();
    }

    public List<Order> getOrderHistory() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getOrders();
    }
    @Transactional
    public Optional<Product> getById(int id) {
        return productDAO.findById(id);
    }
}
