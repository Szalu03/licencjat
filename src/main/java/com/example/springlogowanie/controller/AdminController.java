package com.example.springlogowanie.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class AdminController {
    @GetMapping("/admin/adminpanel")
    public String adminpanel(){
        return "adminpanel";
    }
}
