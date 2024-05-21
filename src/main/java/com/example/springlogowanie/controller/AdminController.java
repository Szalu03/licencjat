package com.example.springlogowanie.controller;

import com.example.springlogowanie.service.BookService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class AdminController {
    private BookService bookService;

    @GetMapping("/admin/adminpanel")
    public String adminpanel(){
        return "adminpanel";
    }

}
