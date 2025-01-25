package com.example.springlogowanie.service;

import com.example.springlogowanie.model.Product;
import com.example.springlogowanie.model.Cart;
import com.example.springlogowanie.model.User;
import com.example.springlogowanie.repository.CartRepository;
import com.example.springlogowanie.repository.ProductDAO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private UserService userService;

    @Transactional
    public void addToCart(int productId, int quantity) {
        User user = userService.getCurrentUser();
        Cart cart = user.getCart();

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cart.addItem(product, quantity);
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(int productId) {
        User user = userService.getCurrentUser();
        Cart cart = user.getCart();

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cart.removeItem(product);
        cartRepository.save(cart);
    }

    @Transactional
    public Cart getCart() {
        User user = userService.getCurrentUser();
        return user.getCart();
    }

    @Transactional
    public Cart saveCart(Cart cart) {

        return cartRepository.save(cart); // Zapisz zaktualizowany koszyk w bazie danych
    }
}

