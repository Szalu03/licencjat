package com.example.springlogowanie.service;

import com.example.springlogowanie.model.*;
import com.example.springlogowanie.repository.ProductDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class TestDataGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductDAO productDAO;

    private Random random = new Random();

    @Transactional
    public void generateTestData() {
        // Poprawna kolejność usuwania: najpierw rekordy zależne, potem nadrzędne
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM CartItem").executeUpdate();
        entityManager.createQuery("DELETE FROM User").executeUpdate(); // Najpierw usuń użytkowników
        entityManager.createQuery("DELETE FROM Cart").executeUpdate(); // Potem koszyki
        entityManager.createQuery("DELETE FROM Product").executeUpdate(); // Na końcu produkty

        // Wstawianie ról tylko jeśli baza jest pusta
        long roleCount = (Long) entityManager.createQuery("SELECT COUNT(r) FROM Role r").getSingleResult();
        if (roleCount == 0) {
            Role roleUser = new Role();
            roleUser.setId(1L);
            roleUser.setName("ROLE_USER");
            entityManager.persist(roleUser);
        }

        // Wstawianie produktów (zawsze po usunięciu, bez ręcznego ustawiania ID)
        List<Product> products = new ArrayList<>();
        String[][] productData = {
                // Smartfony (10)
                {"", "iPhone 14", "Smartfon Apple z ekranem OLED 6.1 cala i procesorem A15 Bionic", "799.99", "50"},
                {"", "Samsung Galaxy S23", "Smartfon Samsung z procesorem Snapdragon 8 Gen 2 i ekranem AMOLED", "699.99", "40"},
                {"", "Xiaomi 13", "Smartfon Xiaomi z aparatem 50 MP i szybkim ładowaniem", "599.99", "30"},
                {"", "Google Pixel 7 Pro", "Smartfon Google z aparatem 50 MP i ekranem AMOLED", "699.99", "35"},
                {"", "Oppo Find X5", "Smartfon Oppo z ekranem AMOLED i szybkim ładowaniem 80W", "649.99", "25"},
                {"", "Sony Xperia 1 IV", "Smartfon Sony z ekranem 4K OLED i aparatem 12 MP", "999.99", "20"},
                {"", "Realme GT Neo 3", "Smartfon Realme z szybkim ładowaniem 150W i ekranem AMOLED", "549.99", "30"},
                {"", "Huawei Mate 50 Pro", "Smartfon Huawei z aparatem 64 MP i ekranem OLED", "899.99", "20"},
                {"", "OnePlus 11", "Smartfon OnePlus z szybkim ładowaniem 100W i ekranem Fluid AMOLED", "749.99", "25"},
                {"", "Nokia G60", "Smartfon Nokia z ekranem 6.5 cala i baterią 4500 mAh", "399.99", "40"},
                // Laptopy (10)
                {"", "MacBook Air M2", "Laptop Apple z procesorem M2 i ekranem Retina", "1199.99", "20"},
                {"", "Dell XPS 13", "Laptop Dell z ekranem 13.4 cala i procesorem Intel i7", "999.99", "25"},
                {"", "HP Spectre x360", "Laptop konwertowalny HP z ekranem OLED i procesorem Intel i5", "1299.99", "15"},
                {"", "Lenovo ThinkPad X1 Carbon", "Laptop biznesowy Lenovo z ekranem 14 cali i procesorem Intel i7", "1399.99", "15"},
                {"", "Asus ROG Zephyrus G14", "Laptop gamingowy Asus z procesorem AMD Ryzen 9 i kartą RTX 3060", "1499.99", "10"},
                {"", "Acer Aspire 5", "Laptop Acer z ekranem 15.6 cala i procesorem Intel i5", "599.99", "30"},
                {"", "Microsoft Surface Laptop 4", "Laptop Microsoft z ekranem 13.5 cala i procesorem Intel i7", "1299.99", "20"},
                {"", "LG Gram 16", "Laptop LG z ekranem 16 cali i ultralekką konstrukcją", "1199.99", "15"},
                {"", "Razer Blade 14", "Laptop gamingowy Razer z procesorem AMD Ryzen 9 i kartą RTX 3070", "1799.99", "10"},
                {"", "HP Pavilion Aero 13", "Laptop HP z ekranem 13.3 cala i procesorem AMD Ryzen 5", "799.99", "25"},
                // Akcesoria komputerowe (10)
                {"", "Logitech MX Master 3", "Myszka bezprzewodowa Logitech z precyzyjnym sensorem", "99.99", "100"},
                {"", "Keychron K8", "Klawiatura mechaniczna Keychron z przełącznikami Gateron i podświetleniem RGB", "79.99", "80"},
                {"", "SanDisk Pendrive 128GB", "Pendrive USB 3.0 SanDisk o pojemności 128GB", "29.99", "200"},
                {"", "Anker Ładowarka 65W", "Uniwersalna ładowarka USB-C Anker o mocy 65W", "49.99", "150"},
                {"", "Kabel USB-C 2m", "Kabel USB-C o długości 2 metrów kompatybilny z szybkim ładowaniem", "9.99", "500"},
                {"", "Logitech G502 Hero", "Myszka gamingowa Logitech z sensorem 25K DPI", "59.99", "120"},
                {"", "Corsair K95 RGB Platinum", "Klawiatura mechaniczna Corsair z przełącznikami Cherry MX", "199.99", "50"},
                {"", "Samsung T7 SSD 1TB", "Zewnętrzny dysk SSD Samsung T7 o pojemności 1TB", "129.99", "80"},
                {"", "Baseus Adapter USB-C", "Adapter USB-C Baseus z portami HDMI i USB", "39.99", "150"},
                {"", "HyperX QuadCast", "Mikrofon USB HyperX z podświetleniem RGB", "139.99", "60"},
                // Słuchawki (5)
                {"", "Bose QuietComfort 45", "Słuchawki Bose z redukcją szumów i trybem ambient", "329.99", "60"},
                {"", "Sony WH-1000XM5", "Słuchawki bezprzewodowe Sony z redukcją szumów i długim czasem pracy", "399.99", "50"},
                {"", "JBL Live 660NC", "Słuchawki bezprzewodowe JBL z redukcją szumów", "199.99", "70"},
                {"", "Apple AirPods Pro 2", "Słuchawki bezprzewodowe Apple z redukcją szumów", "249.99", "80"},
                {"", "Sennheiser Momentum 4", "Słuchawki bezprzewodowe Sennheiser z baterią 60h", "349.99", "40"},
                // Monitory (5)
                {"", "Dell UltraSharp 27", "Monitor 4K Dell 27 cali z matrycą IPS", "499.99", "30"},
                {"", "LG UltraGear 32GP850", "Monitor gamingowy LG 32 cale z matrycą QHD 165Hz", "599.99", "25"},
                {"", "Samsung Odyssey G7", "Monitor gamingowy Samsung 27 cali z matrycą QHD 240Hz", "699.99", "20"},
                {"", "AOC CQ32G2S", "Monitor gamingowy AOC 32 cale z matrycą QHD 144Hz", "399.99", "35"},
                {"", "Asus ProArt PA278CV", "Monitor Asus 27 cali z matrycą QHD dla profesjonalistów", "549.99", "30"},
                // Tablety (5)
                {"", "Samsung Galaxy Tab S8", "Tablet Samsung z ekranem AMOLED i rysikiem S Pen", "699.99", "30"},
                {"", "iPad Air 5", "Tablet Apple z ekranem 10.9 cala i procesorem M1", "599.99", "40"},
                {"", "Lenovo Tab P11 Pro", "Tablet Lenovo z ekranem OLED 11.5 cala", "499.99", "35"},
                {"", "Huawei MatePad 11", "Tablet Huawei z ekranem 120Hz i rysikiem", "449.99", "30"},
                {"", "Microsoft Surface Go 3", "Tablet Microsoft z ekranem 10.5 cala i procesorem Intel", "549.99", "25"},
                // Głośniki (5)
                {"", "JBL Flip 6", "Przenośny głośnik Bluetooth JBL z wodoodporną obudową", "129.99", "70"},
                {"", "Bose SoundLink Revolve+", "Głośnik Bluetooth Bose z dźwiękiem 360 stopni", "299.99", "50"},
                {"", "Sony SRS-XB43", "Głośnik Bluetooth Sony z mocnym basem i podświetleniem", "199.99", "60"},
                {"", "Ultimate Ears Boom 3", "Głośnik Bluetooth UE z wodoodporną obudową", "149.99", "80"},
                {"", "Harman Kardon Onyx Studio 7", "Głośnik Bluetooth Harman Kardon z eleganckim designem", "249.99", "40"}
        };
        for (String[] data : productData) {
            Product product = new Product();
            product.setName(data[1]);
            product.setDescription(data[2]);
            product.setPrice(BigDecimal.valueOf(Double.parseDouble(data[3])));
            product.setStock(Integer.parseInt(data[4]));
            products.add(product);
            entityManager.persist(product); // Zapisz produkt bez ręcznego ustawiania ID
        }

        // Wstawianie użytkowników (losowa liczba od 50 do 150)
        int userCount = 5 + random.nextInt(50);
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setPassword(passwordEncoder.encode("pass" + i));
            Cart cart = new Cart();
            user.setCart(cart);
            // Dodanie roli ROLE_USER
            user.getRoles().add(entityManager.find(Role.class, 1L));
            users.add(user);
            entityManager.persist(user);
            entityManager.persist(cart);
        }

        // Wstawianie elementów koszyka i zamówień dla użytkowników
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            long userId = user.getId();

            // Losowe preferencje dla każdego użytkownika (koszyk i zamówienia)
            int itemCount = 3 + random.nextInt(5); // 3-7 produktów w koszyku
            long[] productIds = new long[itemCount];
            int[] quantities = new int[itemCount];
            for (int j = 0; j < itemCount; j++) {
                productIds[j] = products.get(random.nextInt(products.size())).getId();
                quantities[j] = 1 + random.nextInt(3); // Ilość 1-3
            }
            addCartItems(userId, productIds, quantities, user.getCart());

            // Losowe zamówienia (1-4 na użytkownika)
            int orderCount = 1 + random.nextInt(4); // 1-4 zamówienia
            for (int k = 0; k < orderCount; k++) {
                itemCount = 2 + random.nextInt(4); // 2-5 produktów w zamówieniu
                productIds = new long[itemCount];
                quantities = new int[itemCount];
                for (int j = 0; j < itemCount; j++) {
                    productIds[j] = products.get(random.nextInt(products.size())).getId();
                    quantities[j] = 1 + random.nextInt(3);
                }
                LocalDateTime localDateTime = LocalDateTime.of(2025, 5, 1 + random.nextInt(10), random.nextInt(24), random.nextInt(60));
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                Order order = createOrder(user, date, OrderStatus.COMPLETED);
                orders.add(order);
                addOrderItems(order.getId(), productIds, quantities, order);
            }
        }
    }

    private void addCartItems(long cartId, long[] productIds, int[] quantities, Cart cart) {
        for (int i = 0; i < productIds.length; i++) {
            CartItem item = new CartItem();
            item.setCart(cart);
            Product product = entityManager.find(Product.class, (int) productIds[i]);
            if (product == null) {
                throw new IllegalStateException("Product with ID " + productIds[i] + " not found!");
            }
            item.setProduct(product);
            item.setQuantity(quantities[i]);
            entityManager.persist(item);
        }
    }

    private Order createOrder(User user, Date date, OrderStatus status) {
        Order order = new Order();
        order.setUser(user);
        order.setDate(date);
        order.setStatus(status);
        entityManager.persist(order);
        return order;
    }

    private void addOrderItems(long orderId, long[] productIds, int[] quantities, Order order) {
        for (int i = 0; i < productIds.length; i++) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            Product product = entityManager.find(Product.class, (int) productIds[i]);
            if (product == null) {
                throw new IllegalStateException("Product with ID " + productIds[i] + " not found!");
            }
            item.setProduct(product);
            item.setQuantity(quantities[i]);
            entityManager.persist(item);
        }
    }

    // Getter dla EntityManager (użyty w Application)
    public EntityManager getEntityManager() {
        return entityManager;
    }
}