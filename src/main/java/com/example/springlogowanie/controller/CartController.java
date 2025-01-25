package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Product;
import com.example.springlogowanie.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/add/{productId}/{quantity}")
    public String addToCart(@PathVariable int productId, @PathVariable int quantity) {
        cartService.addToCart(productId, quantity);
        return "redirect:/cart"; // Przekierowanie na stronę koszyka
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable int productId) {
        cartService.removeFromCart(productId);
        return "redirect:/cart"; // Przekierowanie na stronę koszyka
    }

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cart", cartService.getCart());
        return "cart"; // Widok koszyka
    }
}
