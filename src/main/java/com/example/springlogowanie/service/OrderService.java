package com.example.springlogowanie.service;

import com.example.springlogowanie.model.*;
import com.example.springlogowanie.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Order submitOrder() {
        User user = userService.getCurrentUser();
        Cart cart = user.getCart();

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Koszyk jest pusty. Nie można złożyć zamówienia.");
        }

        Order order = new Order();
        order.setDate(new Date());
        order.setStatus(OrderStatus.SUBMITTED);
        order.setUser(user);

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Brak wystarczającej ilości produktu: " + product.getName());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
        }

        cart.getItems().clear();
        cartService.saveCart(cart);
        return orderRepository.save(order);
    }

    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new RuntimeException("Tylko zamówienia w statusie 'SUBMITTED' mogą zostać opłacone.");
        }
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Transactional
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public List<Order> getOrderHistoryForUser(User user) {
        return orderRepository.findByUser(user);
    }
    @Transactional
    public List<Order> getAllOrders() {
        return orderRepository.findAll(); // Pobiera wszystkie zamówienia z bazy danych
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found")); // Znajdź zamówienie po ID
        order.setStatus(OrderStatus.valueOf(status)); // Ustaw nowy status zamówienia
        orderRepository.save(order); // Zapisz zmiany w bazie danych
    }
}
