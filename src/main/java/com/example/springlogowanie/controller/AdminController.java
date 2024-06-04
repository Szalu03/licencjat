package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Order;
import com.example.springlogowanie.model.OrderStatus;
import com.example.springlogowanie.repository.OrderRepository;
import com.example.springlogowanie.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {
    @Autowired
    private BookService bookService;
    @Autowired
    OrderRepository orderRepository;

    @GetMapping("/admin/catalog")
    public String adminpanel(Model model){
        model.addAttribute("books",this.bookService.getAll());
        return "catalog";

    }
    @GetMapping("/admin/adminpanel")
    public String getAllOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "adminpanel";
    }

    @PostMapping("/orders/{id}")
    public String updateOrder(@PathVariable Long id, @RequestParam OrderStatus status, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
        model.addAttribute("order", order);
        return "redirect:/admin/adminpanel";
    }


}
