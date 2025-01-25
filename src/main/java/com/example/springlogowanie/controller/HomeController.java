package com.example.springlogowanie.controller;

import com.example.springlogowanie.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("products", productService.getAll());
        return "home";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        // Załaduj koszyk użytkownika
        model.addAttribute("cart", productService.getCart());
        return "cart";
    }

    @GetMapping("/order/history")
    public String viewOrderHistory(Model model) {
        // Załaduj historię zamówień użytkownika
        model.addAttribute("orderHistory", productService.getOrderHistory());
        return "order-history";
    }
}
