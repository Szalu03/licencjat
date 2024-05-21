package com.example.springlogowanie.repository;

import com.example.springlogowanie.model.Book;

import java.util.List;
import java.util.Optional;

public interface IBookDAO {
    void saveOrUpdate(Book book);
    Optional<Book> getById(int id);
    List<Book> getAll();
    void delete(int id);
}
