package com.example.springlogowanie.controller;

import com.example.springlogowanie.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class AdminController {
    @Autowired
    private BookService bookService;

    @GetMapping("/admin/adminpanel")
    public String adminpanel(Model model){
        model.addAttribute("books",this.bookService.getAll());
        return "adminpanel";

    }

}
