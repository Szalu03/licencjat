package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Book;
import com.example.springlogowanie.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/book")
public class BookController {
    @Autowired
    private BookService bookService;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    // @PostMapping("/add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(@ModelAttribute Book book) {
        this.bookService.saveOrUpdate(book);
        return "redirect:/admin/catalog";
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.GET)
    public String update(@PathVariable int id, Model model) {
        Optional<Book> bookOptional = this.bookService.getById(id);
        if (bookOptional.isEmpty()) {
            return "redirect:/admin/catalog";
        }
        else {
            model.addAttribute("book", bookOptional.get());
            return "book-form";
        }
    }
    @RequestMapping(path = "/update/{id}", method = RequestMethod.POST)
    public String update(@PathVariable int id, @ModelAttribute Book book) {
        this.bookService.saveOrUpdate(book);
        return "redirect:/admin/catalog";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam int id) {
        bookService.delete(id);
        return "redirect:/admin/catalog";
    }


}