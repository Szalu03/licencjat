package com.example.springlogowanie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Setter
@Getter
@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "cart")
    private User user;

    public void addItem(Product product, int quantity) {
        for (CartItem item : this.items) {
            if (item.getProduct().equals(product)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setCart(this);
        this.items.add(cartItem);
    }

    public void removeItem(Product product) {
        for (Iterator<CartItem> iterator = this.items.iterator(); iterator.hasNext(); ) {
            CartItem item = iterator.next();
            if (item.getProduct().equals(product)) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    iterator.remove();
                }
                return;
            }
        }
        throw new RuntimeException("Product not found in cart");
    }
}
