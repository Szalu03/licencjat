package com.example.springlogowanie.service;

import com.example.springlogowanie.model.*;
import com.example.springlogowanie.repository.ProductDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.hibernate.Hibernate;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;



import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserService userService;

    @Autowired
    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Transactional
    public void save(Product product) {
        productDAO.save(product);
    }

    @Transactional
    public List<Product> getAll() {
        return productDAO.findAll();
    }

    @Transactional
    public List<Product> searchProducts(String query) {
        return productDAO.findByNameContainingIgnoreCase(query);
    }

    @Transactional
    public void delete(int id) {
        productDAO.deleteById(id);
    }

    public Cart getCart() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getCart();
    }

    public List<Order> getOrderHistory() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getOrders();
    }

    @Transactional
    public Optional<Product> getById(int id) {
        return productDAO.findById(id);
    }






//    @Transactional
//    public List<Product> getRecommendations(Long userId) {
//        User currentUser = userService.getCurrentUser();
//        if (!currentUser.getId().equals(userId)) {
//            userId = currentUser.getId();
//        }
//        logger.info("Starting recommendation process for userId: {}", userId);
//        System.out.println("Starting recommendation process for userId: " + userId);
//
//        // 1. Przygotowanie danych dla Mahout
//        List<User> allUsers = userService.getAllUsers();
//        logger.debug("Total users retrieved: {}", allUsers.size());
//        System.out.println("Total users retrieved: " + allUsers.size());
//        Map<Long, List<Preference>> preferencesMap = new HashMap<>();
//
//        for (User user : allUsers) {
//            List<Preference> userPrefs = new ArrayList<>();
//            List<Long> cartItems = new ArrayList<>();
//            Cart cart = user.getCart();
//            if (cart != null && cart.getItems() != null) {
//                cart.getItems().forEach(item -> {
//                    userPrefs.add(new GenericPreference(user.getId(), item.getProduct().getId(), 1.0f));
//                    cartItems.add((long) item.getProduct().getId());
//                });
//            }
//            List<Long> orderItems = new ArrayList<>();
//            List<Order> orders = entityManager.createQuery(
//                            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
//                    .setParameter("userId", user.getId())
//                    .getResultList();
//            System.out.println("User " + user.getId() + " has " + orders.size() + " orders");
//            if (orders != null) {
//                orders.forEach(order -> {
//                    List<OrderItem> items = order.getItems();
//                    if (items != null && !items.isEmpty()) {
//                        System.out.println("Order " + order.getId() + " has " + items.size() + " items");
//                        items.forEach(item -> {
//                            userPrefs.add(new GenericPreference(user.getId(), item.getProduct().getId(), 1.0f));
//                            orderItems.add((long) item.getProduct().getId());
//                        });
//                    } else {
//                        System.out.println("Order " + order.getId() + " has no items");
//                    }
//                });
//            }
//            if (!userPrefs.isEmpty()) {
//                preferencesMap.put(user.getId(), userPrefs);
//                logger.debug("User {} - Cart items: {}, Order items: {}, Total preferences: {}",
//                        user.getId(), cartItems, orderItems, userPrefs.stream().map(p -> p.getItemID()).collect(Collectors.toList()));
//                System.out.println("User " + user.getId() + " - Cart items: " + cartItems + ", Order items: " + orderItems +
//                        ", Total preferences: " + userPrefs.stream().map(p -> p.getItemID()).collect(Collectors.toList()));
//            }
//        }
//
//        if (preferencesMap.size() < 2) {
//            logger.warn("Not enough users with preferences (required: 2, found: {}). Returning empty list.", preferencesMap.size());
//            System.out.println("Not enough users with preferences: " + preferencesMap.size());
//            return new ArrayList<>();
//        }
//
//        // 2. Tworzenie DataModel
//        FastByIDMap<PreferenceArray> userData = new FastByIDMap<>();
//        for (Map.Entry<Long, List<Preference>> entry : preferencesMap.entrySet()) {
//            PreferenceArray prefs = new GenericUserPreferenceArray(entry.getValue().size());
//            for (int i = 0; i < entry.getValue().size(); i++) {
//                prefs.set(i, entry.getValue().get(i));
//            }
//            userData.put(entry.getKey(), prefs);
//        }
//        DataModel dataModel = new GenericDataModel(userData);
//        logger.debug("DataModel created with {} users.", userData.size());
//        System.out.println("DataModel created with " + userData.size() + " users.");
//
//        // 3. Konfiguracja Mahout i generowanie rekomendacji
//        try {
//            logger.debug("Initializing similarity calculation...");
//            System.out.println("Initializing similarity calculation...");
//            UserSimilarity similarity = new TanimotoCoefficientSimilarity(dataModel);
//            System.out.println("Similarity szalu (1) vs kasia (2): " + similarity.userSimilarity(1, 2));
//            System.out.println("Similarity szalu (1) vs janek (4): " + similarity.userSimilarity(1, 4));
//
//            logger.debug("Initializing neighborhood...");
//            System.out.println("Initializing neighborhood...");
//            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
//            long[] neighbors = neighborhood.getUserNeighborhood(userId);
//            System.out.println("Neighbors for userId " + userId + ": " + Arrays.toString(neighbors));
//
//            logger.debug("Initializing recommender...");
//            System.out.println("Initializing recommender...");
//            Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
//            logger.debug("Generating recommendations...");
//            System.out.println("Generating recommendations...");
//            List<RecommendedItem> recommendations = recommender.recommend(userId, 5);
//
//            System.out.println("All recommended items:");
//            for (RecommendedItem item : recommendations) {
//                System.out.println("Item ID: " + item.getItemID() + ", Value: " + item.getValue());
//            }
//
//            if (recommendations.isEmpty()) {
//                logger.info("No recommendations generated for userId {}.", userId);
//                System.out.println("No recommendations generated for userId " + userId);
//            } else {
//                List<Integer> recommendedIds = recommendations.stream()
//                        .map(item -> Math.toIntExact(item.getItemID()))
//                        .collect(Collectors.toList());
//                logger.info("Generated {} recommendations for userId {}: {}", recommendedIds.size(), userId, recommendedIds);
//                System.out.println("Generated " + recommendedIds.size() + " recommendations for userId " + userId + ": " + recommendedIds);
//                return productDAO.findAllById(recommendedIds);
//            }
//            return new ArrayList<>();
//        } catch (Exception e) {
//            logger.error("Failed to generate recommendations for userId {}: {}", userId, e.getMessage(), e);
//            System.err.println("Error for userId " + userId + ": " + e.getMessage());
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
@Transactional
public List<Product> getRecommendations(Long userId) {
    User currentUser = userService.getCurrentUser();
    if (!currentUser.getId().equals(userId)) {
        userId = currentUser.getId();
    }
    logger.info("Starting recommendation process for userId: {}", userId);
    System.out.println("Starting recommendation process for userId: " + userId);

    // 1. Pobierz preferencje użytkownika
    Map<Long, Set<Integer>> userPreferences = getAllUserPreferences();
    Set<Integer> targetPrefs = userPreferences.getOrDefault(userId, new HashSet<>());
    if (targetPrefs.isEmpty()) {
        System.out.println("No preferences for userId " + userId);
        return new ArrayList<>();
    }
    System.out.println("User " + userId + " preferences: " + targetPrefs);

    // 2. Pobierz wszystkie produkty
    List<Product> allProducts = productDAO.findAll();
    if (allProducts.isEmpty()) {
        System.out.println("No products found in database!");
        logger.info("No products found in database for userId: {}", userId);
        return new ArrayList<>();
    }
    System.out.println("All products: " + allProducts.stream()
            .map(p -> p.getId() + ": " + p.getName())
            .collect(Collectors.toList()));
    Map<Integer, Product> productMap = allProducts.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

    // 3. Przygotuj indeks Lucene
    RAMDirectory index = new RAMDirectory();
    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);

    try (IndexWriter writer = new IndexWriter(index, config)) {
        for (Product p : allProducts) {
            Document doc = new Document();
            doc.add(new TextField("name", p.getName() != null ? p.getName() : "", Field.Store.YES));
            doc.add(new TextField("description", p.getDescription() != null ? p.getDescription() : "", Field.Store.YES));
            doc.add(new TextField("id", String.valueOf(p.getId()), Field.Store.YES));
            writer.addDocument(doc);
        }
        System.out.println("Lucene index created with " + allProducts.size() + " products");
    } catch (IOException e) {
        System.out.println("Error creating Lucene index: " + e.getMessage());
        e.printStackTrace();
        return new ArrayList<>();
    }

    // 4. Stwórz zapytanie Lucene
    StringBuilder queryStr = new StringBuilder();
    for (Integer productId : targetPrefs) {
        Product p = productMap.get(productId);
        if (p != null) {
            if (p.getName() != null) queryStr.append(p.getName()).append(" ");
            if (p.getDescription() != null) queryStr.append(p.getDescription()).append(" ");
        }
    }
    String queryString = queryStr.toString().trim();
    System.out.println("Query string: " + queryString);
    if (queryString.isEmpty()) {
        System.out.println("Query string is empty!");
        return allProducts.stream()
                .filter(p -> !targetPrefs.contains(p.getId()))
                .limit(3)
                .collect(Collectors.toList());
    }

    // 5. Wyszukaj podobne produkty w obu polach (name i description)
    List<Integer> recommendedIds = new ArrayList<>();
    try (DirectoryReader reader = DirectoryReader.open(index)) {
        IndexSearcher searcher = new IndexSearcher(reader);
        String[] fields = {"name", "description"};
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, fields, analyzer);
        Query query = parser.parse(queryString); // Bez escape, żeby zachować elastyczność
        System.out.println("Lucene query: " + query.toString());

        TopDocs topDocs = searcher.search(query, 10);
        System.out.println("Found " + topDocs.scoreDocs.length + " matches");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            Integer productId = Integer.parseInt(doc.get("id"));
            float score = scoreDoc.score;
            System.out.println("Match: " + productId + " (" + doc.get("name") + ") - Score: " + score);
            recommendedIds.add(productId);
        }
    } catch (Exception e) {
        System.out.println("Error searching Lucene index: " + e.getMessage());
        e.printStackTrace();
        return allProducts.stream()
                .filter(p -> !targetPrefs.contains(p.getId()))
                .limit(3)
                .collect(Collectors.toList());
    }

    // 6. Filtruj i ogranicz rekomendacje
    List<Integer> finalRecommendedIds = recommendedIds.stream()
            .filter(id -> !targetPrefs.contains(id))
            .distinct()
            .limit(3)
            .collect(Collectors.toList());

    if (finalRecommendedIds.isEmpty()) {
        System.out.println("No unique recommendations found, falling back to random products");
        finalRecommendedIds = allProducts.stream()
                .filter(p -> !targetPrefs.contains(p.getId()))
                .map(Product::getId)
                .sorted(Comparator.comparingInt(id -> (int) (Math.random() * 1000)))
                .limit(3)
                .collect(Collectors.toList());
    }

    // 7. Zwróć rekomendacje
    if (finalRecommendedIds.isEmpty()) {
        logger.info("No recommendations generated for userId {}.", userId);
        System.out.println("No recommendations generated for userId " + userId);
        return new ArrayList<>();
    } else {
        logger.info("Generated {} recommendations for userId {}: {}", finalRecommendedIds.size(), userId, finalRecommendedIds);
        System.out.println("Generated " + finalRecommendedIds.size() + " recommendations for userId " + userId + ": " + finalRecommendedIds);
        return productDAO.findAllById(finalRecommendedIds);
    }
}

    private Map<Long, Set<Integer>> getAllUserPreferences() {
        Map<Long, Set<Integer>> userPreferences = new HashMap<>();
        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            Set<Integer> prefs = new HashSet<>();
            Cart cart = user.getCart();
            if (cart != null && cart.getItems() != null) {
                cart.getItems().forEach(item -> prefs.add(item.getProduct().getId()));
            }
            List<Order> orders = entityManager.createQuery(
                            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
            orders.forEach(order -> order.getItems().forEach(item -> prefs.add(item.getProduct().getId())));
            userPreferences.put(user.getId(), prefs);
        }
        return userPreferences;
    }

//    private Map<Long, Set<Long>> getAllUserPreferences() {
//        Map<Long, Set<Long>> userPreferences = new HashMap<>();
//        List<User> allUsers = userService.getAllUsers();
//        for (User user : allUsers) {
//            Set<Long> prefs = new HashSet<>();
//            Cart cart = user.getCart();
//            if (cart != null && cart.getItems() != null) {
//                cart.getItems().forEach(item -> prefs.add((long) item.getProduct().getId()));
//            }
//            List<Order> orders = entityManager.createQuery(
//                            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
//                    .setParameter("userId", user.getId())
//                    .getResultList();
//            orders.forEach(order -> order.getItems().forEach(item -> prefs.add((long) item.getProduct().getId())));
//            userPreferences.put(user.getId(), prefs);
//        }
//        return userPreferences;
//    }
//}


    // Klasa pomocnicza do tworzenia preferencji
    private static class GenericPreference implements Preference {
        private final long userId;
        private final long itemId;
        private final float value;

        public GenericPreference(long userId, long itemId, float value) {
            this.userId = userId;
            this.itemId = itemId;
            this.value = value;
        }

        @Override
        public long getUserID() {
            return userId;
        }

        @Override
        public long getItemID() {
            return itemId;
        }

        @Override
        public float getValue() {
            return value;
        }

        @Override
        public void setValue(float value) {
            // Nie używane w naszym przypadku
        }
    }
}