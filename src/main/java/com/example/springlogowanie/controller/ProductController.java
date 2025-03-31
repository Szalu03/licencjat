package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Product;
import com.example.springlogowanie.repository.ProductDAO;
import com.example.springlogowanie.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductDAO productDAO;

    // Jedna metoda dla /products/{id}
    @GetMapping("/{id}")
    public String getProductDetails(@PathVariable("id") Integer id, Model model) {
        Product product = productDAO.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/products"; // Jeśli produkt nie istnieje, przekieruj
        }
        model.addAttribute("product", product);
        return "product-detail"; // Jednolita nazwa szablonu
    }

    @GetMapping
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAll();
        model.addAttribute("products", products);
        return "products";
    }

    @PostMapping
    public String addProduct(@ModelAttribute Product product) {
        productService.save(product);
        return "redirect:/products";
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable int id) {
        productService.delete(id);
        return "redirect:/products";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, Model model) {
        List<Product> products = productService.searchProducts(query);
        model.addAttribute("products", products);
        return "products";
    }
}