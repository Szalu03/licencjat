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
    UserService userService;
    @Autowired
    CartService cartservice;
    @Autowired
    OrderRepository orderRepository;

    @Transactional
    public Order submitOrder() {

        User user = userService.getCurrentUser();
        Cart cart = user.getCart();
        Order order = new Order();
        order.setDate(new Date());
        order.setStatus(OrderStatus.SUBMITTED);
        order.setUser(user);
        for (CartItem cartItem : cart.getItems()) {
            Book book = cartItem.getBook();
            if (book.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for book: " + book.getTitle());
            }
            OrderItem orderItem = new OrderItem();

            orderItem.setBook(cartItem.getBook());

            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
        }
        cart.getItems().clear();
        cartservice.saveCart(cart);

        return orderRepository.save(order);
    }
    @Transactional
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(()
        -> new RuntimeException("Order not found"));

    }
    @Transactional
    public void saveOrder (Order order) {
        orderRepository.save(order);
    }
    @Transactional
    public List<Order> getOrderHistoryForUser(User user) {
        return orderRepository.findByUser(user);
    }
}
