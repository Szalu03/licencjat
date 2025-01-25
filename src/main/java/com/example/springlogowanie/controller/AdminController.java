package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Order;
import com.example.springlogowanie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/adminpanel")
    public String getAllOrders(Model model) {
        List<Order> orders = orderService.getAllOrders(); // Pobierz wszystkie zamówienia
        model.addAttribute("orders", orders);
        return "adminpanel"; // Wyświetl widok panelu administratora
    }

    @PostMapping("/orders/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status); // Zaktualizuj status zamówienia
        return "redirect:/admin/adminpanel"; // Przekierowanie na panel admina
    }
}
