package com.example.springlogowanie.service;

import com.example.springlogowanie.model.Book;
import com.example.springlogowanie.repository.IBookDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService implements IBookService {
    @Autowired
    private final IBookDAO bookDAO;
    private  EntityManager entityManager;
    private final String GET_BY_ID_JPQL = "Select b FROM com.example.springlogowanie.model.Book b WHERE b.id = :id";
    public BookService (IBookDAO bookDAO) { this.bookDAO = bookDAO;
    }
    @Override
    @Transactional
    public Optional<Book> getById(int id) {
        TypedQuery<Book> query = entityManager.createQuery(GET_BY_ID_JPQL,Book.class);
        query.setParameter("id", id);

        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    @Override
    @Transactional
    public List<Book> getAll() { return this.bookDAO.getAll();
    }
    @Transactional
    public void saveOrUpdate(Book book) {
        System.out.println("B00K "+book); if (getById(book.getId()).isEmpty()) {
            entityManager.persist(book);
        } else {
            entityManager.merge(book);
        }
    }
    @Override
    @Transactional
    public void delete(int id) {
        Book book = getById(id).orElse(null);
        if (book != null){
            entityManager.remove(book);
        }
    }
}