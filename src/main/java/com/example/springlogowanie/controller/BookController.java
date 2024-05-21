package com.example.springlogowanie.controller;

import com.example.springlogowanie.model.Book;
import com.example.springlogowanie.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping(path = "/book")
public class BookController {
    private BookService bookService;
    @RequestMapping(path = "/add",method = RequestMethod.GET )
    public String add(Model model){
        model.addAttribute("book", new Book());
        return "book-form";
    }
    @RequestMapping(path = "/add",method = RequestMethod.POST )
    public String add(@ModelAttribute Book book){
        this.bookService.saveOrUpdate(book);
        return "redirect:/main";
    }
    @RequestMapping(path = "/update/{id}", method = RequestMethod.GET)
    public String update (@PathVariable int id, Model model) {
        Optional<Book> book0pt = this.bookService.getById(id);
        if(book0pt.isEmpty()) {
            return "redirect:/main";
        }
        model.addAttribute("book", book0pt.get());
        return "book-form";
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.POST)
    public String update (@PathVariable int id, @ModelAttribute Book book) {
        //book.setId(id);
        this.bookService.saveOrUpdate(book);
        return "redirect:/main";
    }
    @PostMapping("/delete")
    public String deleteBook(@RequestParam int id){
        bookService.delete(id);
        return "redirect:/main";
    }
}
