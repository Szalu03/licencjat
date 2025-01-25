package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Order;
import com.example.springlogowanie.model.User;
import com.example.springlogowanie.service.OrderService;
import com.example.springlogowanie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public String submitOrder(RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.submitOrder();
            redirectAttributes.addFlashAttribute("successMessage", "Zamówienie zostało złożone.");
            return "redirect:/order/" + order.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/{orderId}")
    public String getOrder(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrder(orderId);
        model.addAttribute("order", order);
        return "order";
    }

    @PostMapping("/pay/{orderId}")
    public String payOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.payOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Zamówienie zostało opłacone.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/order/" + orderId;
    }

    @GetMapping("/history")
    public String getOrderHistory(Model model) {
        User currentUser = userService.getCurrentUser();
        List<Order> orderHistory = orderService.getOrderHistoryForUser(currentUser);
        model.addAttribute("orderHistory", orderHistory);
        return "orderhistory";
    }
}
