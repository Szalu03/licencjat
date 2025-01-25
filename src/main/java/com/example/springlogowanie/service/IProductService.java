package com.example.springlogowanie.service;

import com.example.springlogowanie.model.Product;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    void saveOrUpdate(Product product);
    Optional<Product> getById(int id);
    List<Product> getAll();
    void delete(int id);
}
