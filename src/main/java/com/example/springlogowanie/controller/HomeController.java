package com.example.springlogowanie.controller;

import com.example.springlogowanie.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String home(Model model) {
        // Pobierz wszystkie produkty
        model.addAttribute("products", productService.getAll());
        // Pobierz rekomendowane produkty dla bieżącego użytkownika
        model.addAttribute("recommendedProducts", productService.getRecommendations());
        return "home";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        model.addAttribute("cart", productService.getCart());
        return "cart";
    }

    @GetMapping("/order/history")
    public String viewOrderHistory(Model model) {
        model.addAttribute("orderHistory", productService.getOrderHistory());
        return "order-history";
    }
}