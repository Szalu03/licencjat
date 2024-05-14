package com.example.springlogowanie.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/home","/"})
    public String home() {
        return "home";
    }

    @GetMapping("/admin/adminpanel")
    public String adminpanel() {
        return "adminpanel";
    }
}
