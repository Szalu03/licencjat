package com.example.springlogowanie.service;

import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import java.io.IOException;
import com.example.springlogowanie.model.*;
import com.example.springlogowanie.repository.ProductDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    private final boolean useCollaborativeFiltering = true;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
        MetricsCalculator.initializeLogFile();
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


    @Transactional
    public List<Product> getRecommendations() {
        User currentUser = userService.getCurrentUser();
        long userId = currentUser.getId();
        if (useCollaborativeFiltering) {
            return getCollaborativeRecommendations(userId);

        } else {

            return getContentBasedRecommendations(userId);
        }
    }

    // Pobieranie prawdy gruntowej (produkty z zamówień i koszyka użytkownika)
    private Set<Integer> getGroundTruth(Long userId) {
        Set<Integer> groundTruth = new HashSet<>();
        User user = userService.getCurrentUser();
        Cart cart = user.getCart();
        if (cart != null && cart.getItems() != null) {
            cart.getItems().forEach(item -> groundTruth.add(item.getProduct().getId()));
        }
        List<Order> orders = entityManager.createQuery(
                        "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
                .setParameter("userId", userId)
                .getResultList();
        orders.forEach(order -> order.getItems().forEach(item -> groundTruth.add(item.getProduct().getId())));
        logger.debug("Ground truth for userId {}: {}", userId, groundTruth);
        System.out.println("Ground truth for userId " + userId + ": " + groundTruth);
        return groundTruth;
    }

     //dla Collaborative Filtering
    private Set<Integer> getCollaborativeGroundTruth(Long userId) {
        Set<Integer> groundTruth = new HashSet<>();
        List<User> allUsers = userService.getAllUsers();
        Map<Long, List<Preference>> preferencesMap = new HashMap<>();

        // Pobieramy preferencje wszystkich użytkowników
        for (User user : allUsers) {
            List<Preference> userPrefs = new ArrayList<>();
            Cart cart = user.getCart();
            if (cart != null && cart.getItems() != null) {
                cart.getItems().forEach(item -> userPrefs.add(new GenericPreference(user.getId(), item.getProduct().getId(), 1.0f)));
            }
            List<Order> orders = entityManager.createQuery(
                            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
            orders.forEach(order -> order.getItems().forEach(item -> userPrefs.add(new GenericPreference(user.getId(), item.getProduct().getId(), 1.0f))));
            if (!userPrefs.isEmpty()) {
                preferencesMap.put(user.getId(), userPrefs);
            }
        }

        if (preferencesMap.size() < 2) {
            logger.warn("Not enough users with preferences to generate groundTruth: {}", preferencesMap.size());
            System.out.println("Not enough users with preferences to generate groundTruth: " + preferencesMap.size());
            return groundTruth;
        }

        FastByIDMap<PreferenceArray> userData = new FastByIDMap<>();
        for (Map.Entry<Long, List<Preference>> entry : preferencesMap.entrySet()) {
            PreferenceArray prefs = new GenericUserPreferenceArray(entry.getValue().size());
            for (int i = 0; i < entry.getValue().size(); i++) {
                prefs.set(i, entry.getValue().get(i));
            }
            userData.put(entry.getKey(), prefs);
        }
        DataModel dataModel = new GenericDataModel(userData);

        UserSimilarity similarity = new TanimotoCoefficientSimilarity(dataModel);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.05, similarity, dataModel);
        long[] neighbors = new long[0];
        try {
            neighbors = neighborhood.getUserNeighborhood(userId);
        } catch (TasteException e) {
            logger.error("Error finding neighbors for userId {}: {}", userId, e.getMessage(), e);
            System.out.println("Error finding neighbors for userId " + userId + ": " + e.getMessage());
        }

        // Dodajemy do groundTruth produkty, które są popularne wśród sąsiadów
        for (long neighborId : neighbors) {
            List<Preference> neighborPrefs = preferencesMap.get(neighborId);
            if (neighborPrefs != null) {
                neighborPrefs.forEach(pref -> groundTruth.add((int) pref.getItemID()));
            }
        }

        logger.debug("Collaborative ground truth for userId {}: {}", userId, groundTruth);
        System.out.println("Collaborative ground truth for userId " + userId + ": " + groundTruth);
        return groundTruth;
    }
//collab
    @Transactional
    public List<Product> getCollaborativeRecommendations(long userId) {
        User currentUser = userService.getCurrentUser();
        final long effectiveUserId = currentUser.getId().equals(userId) ? userId : currentUser.getId();
        logger.info("Starting Collaborative Filtering recommendation for userId: {}", effectiveUserId);
        System.out.println("Starting Collaborative Filtering recommendation for userId: " + effectiveUserId);

        List<Integer> recommendedIds = new ArrayList<>();
        Set<Integer> groundTruth = getCollaborativeGroundTruth(effectiveUserId);
        System.out.println("Collaborative ground truth for userId " + effectiveUserId + ": " + groundTruth);

        int totalUsers = userService.getAllUsers().size();

        MetricsCalculator.Metrics metrics = MetricsCalculator.measureMetrics(() -> {
            List<Integer> localRecommendedIds = new ArrayList<>();
            int localUsersWithRecommendations = 0;

            try {
                List<User> allUsers = userService.getAllUsers();
                logger.debug("Total users retrieved: {}", allUsers.size());
                System.out.println("Total users retrieved: " + allUsers.size());
                Map<Long, List<Preference>> preferencesMap = new HashMap<>();

                for (User user : allUsers) {
                    List<Preference> userPrefs = new ArrayList<>();
                    List<Long> cartItems = new ArrayList<>();
                    Cart cart = user.getCart();
                    if (cart != null && cart.getItems() != null) {
                        cart.getItems().forEach(item -> {
                            userPrefs.add(new GenericPreference(user.getId(), item.getProduct().getId(), 1.0f));
                            cartItems.add((long) item.getProduct().getId());
                        });
                    }
                    List<Long> orderItems = new ArrayList<>();
                    List<Order> orders = entityManager.createQuery(
                                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
                            .setParameter("userId", user.getId())
                            .getResultList();
                    System.out.println("User " + user.getId() + " has " + orders.size() + " orders");
                    if (orders != null) {
                        orders.forEach(order -> {
                            List<OrderItem> items = order.getItems();
                            if (items != null && !items.isEmpty()) {
                                System.out.println("Order " + order.getId() + " has " + items.size() + " items");
                                items.forEach(item -> {
                                    userPrefs.add(new GenericPreference(user.getId(), item.getProduct().getId(), 1.0f));
                                    orderItems.add((long) item.getProduct().getId());
                                });
                            }
                        });
                    }
                    if (!userPrefs.isEmpty()) {
                        preferencesMap.put(user.getId(), userPrefs);
                        logger.debug("User {} - Cart items: {}, Order items: {}, Total preferences: {}",
                                user.getId(), cartItems, orderItems, userPrefs.stream().map(p -> p.getItemID()).collect(Collectors.toList()));
                        System.out.println("User " + user.getId() + " - Cart items: " + cartItems + ", Order items: " + orderItems +
                                ", Total preferences: " + userPrefs.stream().map(p -> p.getItemID()).collect(Collectors.toList()));
                    }
                }

                if (preferencesMap.size() < 2) {
                    logger.warn("Not enough users with preferences: {}", preferencesMap.size());
                    System.out.println("Not enough users with preferences: " + preferencesMap.size());
                    localRecommendedIds.addAll(getRandomProducts(groundTruth, 5));
                    recommendedIds.addAll(localRecommendedIds);
                    if (!localRecommendedIds.isEmpty()) {
                        localUsersWithRecommendations = 1;
                    }
                    return localUsersWithRecommendations;
                }

                FastByIDMap<PreferenceArray> userData = new FastByIDMap<>();
                for (Map.Entry<Long, List<Preference>> entry : preferencesMap.entrySet()) {
                    PreferenceArray prefs = new GenericUserPreferenceArray(entry.getValue().size());
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        prefs.set(i, entry.getValue().get(i));
                    }
                    userData.put(entry.getKey(), prefs);
                }
                DataModel dataModel = new GenericDataModel(userData);
                logger.debug("DataModel created with {} users.", userData.size());
                System.out.println("DataModel created with " + userData.size() + " users.");

                UserSimilarity similarity = new TanimotoCoefficientSimilarity(dataModel);
                System.out.println("Similarity calculation initialized");
                UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.01, similarity, dataModel); // Obniżony próg
                System.out.println("Neighborhood initialized");
                long[] neighbors = neighborhood.getUserNeighborhood(effectiveUserId);
                System.out.println("Neighbors for userId " + effectiveUserId + ": " + Arrays.toString(neighbors));

                Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
                System.out.println("Recommender initialized");
                List<RecommendedItem> recommendations = recommender.recommend(effectiveUserId, 10); // Więcej rekomendacji
                System.out.println("All raw recommendations:");
                for (RecommendedItem item : recommendations) {
                    System.out.println("Item ID: " + item.getItemID() + ", Value: " + item.getValue());
                }

                if (!recommendations.isEmpty()) {
                    localRecommendedIds.addAll(recommendations.stream()
                            .map(item -> Math.toIntExact(item.getItemID()))
                            .filter(id -> !getGroundTruth(effectiveUserId).contains(id)) // Exclude already preferred items
                            .collect(Collectors.toList()));
                    localUsersWithRecommendations = 1;
                } else {
                    logger.warn("No recommendations generated for userId {}. Falling back to random products.", effectiveUserId);
                    System.out.println("No recommendations generated for userId " + effectiveUserId + ". Falling back to random products.");
                    localRecommendedIds.addAll(getRandomProducts(groundTruth, 5));
                    if (!localRecommendedIds.isEmpty()) {
                        localUsersWithRecommendations = 1;
                    }
                }

                recommendedIds.addAll(localRecommendedIds.stream().distinct().limit(5).collect(Collectors.toList())); // Więcej wyników
                return localUsersWithRecommendations;
            } catch (TasteException e) {
                logger.error("Error generating recommendations for userId {}: {}", effectiveUserId, e.getMessage(), e);
                System.err.println("Error generating recommendations for userId " + effectiveUserId + ": " + e.getMessage());
                e.printStackTrace();
                localRecommendedIds.addAll(getRandomProducts(groundTruth, 5));
                if (!localRecommendedIds.isEmpty()) {
                    return 1;
                }
                return 0;
            }
        }, "CollaborativeFiltering", recommendedIds, groundTruth, totalUsers);

        logger.info("Collaborative Filtering Metrics: F1-Score: {}, Coverage: {}, Time: {} ms",
                metrics.f1Score, metrics.coverage, metrics.executionTime);
        System.out.printf("Collaborative Filtering Metrics: F1-Score: %.4f, Coverage: %.4f, Time: %d ms\n",
                metrics.f1Score, metrics.coverage, metrics.executionTime);

        return productDAO.findAllById(recommendedIds);
    }

    // Metoda do pobierania losowych produktów jako fallback
    private List<Integer> getRandomProducts(Set<Integer> excludeIds, int limit) {
        List<Product> allProducts = productDAO.findAll();
        List<Integer> randomIds = allProducts.stream()
                .map(Product::getId)
                .filter(id -> !excludeIds.contains(id))
                .collect(Collectors.toList());
        Collections.shuffle(randomIds, new Random());
        return randomIds.stream().limit(limit).collect(Collectors.toList());
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
            // Nie używane
        }
    }



    @Transactional
    public List<Product> getContentBasedRecommendations(Long userId) {
        User currentUser = userService.getCurrentUser();
        final Long effectiveUserId = currentUser.getId().equals(userId) ? userId : currentUser.getId();
        logger.info("Starting Content-Based recommendation for userId: {}", effectiveUserId);
        System.out.println("Starting Content-Based recommendation for userId: " + effectiveUserId);

        List<Integer> recommendedIds = new ArrayList<>();
        Set<Integer> groundTruth = getDynamicGroundTruth(effectiveUserId);
        System.out.println("Dynamic ground truth for userId " + effectiveUserId + ": " + groundTruth);

        int totalUsers = userService.getAllUsers().size();

        MetricsCalculator.Metrics metrics = MetricsCalculator.measureMetrics(() -> {
            List<Integer> localRecommendedIds = new ArrayList<>();
            int localUsersWithRecommendations = 0; // Lokalna zmienna w lambdzie

            Map<Long, Set<Integer>> userPreferences = getAllUserPreferences();
            Set<Integer> targetPrefs = userPreferences.getOrDefault(effectiveUserId, new HashSet<>());
            if (targetPrefs.isEmpty()) {
                logger.info("No preferences for userId {}", effectiveUserId);
                System.out.println("No preferences for userId " + effectiveUserId);
                return 0; // Zwracamy 0, jeśli brak preferencji
            }
            System.out.println("User " + effectiveUserId + " preferences: " + targetPrefs);

            List<Product> allProducts = productDAO.findAll();
            if (allProducts.isEmpty()) {
                logger.info("No products found in database for userId: {}", effectiveUserId);
                System.out.println("No products found in database!");
                return 0;
            }
            System.out.println("All products: " + allProducts.stream()
                    .map(p -> p.getId() + ": " + p.getName())
                    .collect(Collectors.toList()));
            Map<Integer, Product> productMap = allProducts.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

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
                logger.error("Error creating Lucene index: {}", e.getMessage(), e);
                System.out.println("Error creating Lucene index: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }

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
                return 0;
            }

            try (DirectoryReader reader = DirectoryReader.open(index)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                String[] fields = {"name", "description"};
                MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, fields, analyzer);
                Query query = parser.parse(queryString);
                System.out.println("Lucene query: " + query.toString());

                TopDocs topDocs = searcher.search(query, 10);
                System.out.println("Found " + topDocs.scoreDocs.length + " matches");
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    Integer productId = Integer.parseInt(doc.get("id"));
                    float score = scoreDoc.score;
                    System.out.println("Match: " + productId + " (" + doc.get("name") + ") - Score: " + score);
                    localRecommendedIds.add(productId);
                }

                localRecommendedIds = localRecommendedIds.stream()
                        .filter(id -> !targetPrefs.contains(id))
                        .distinct()
                        .limit(3)
                        .collect(Collectors.toList());

                if (!localRecommendedIds.isEmpty()) {
                    localUsersWithRecommendations = 1;
                } else {
                    System.out.println("No unique recommendations found, falling back to random products");
                    localRecommendedIds.addAll(allProducts.stream()
                            .filter(p -> !targetPrefs.contains(p.getId()))
                            .map(Product::getId)
                            .sorted(Comparator.comparingInt(id -> (int) (Math.random() * 1000)))
                            .limit(3)
                            .collect(Collectors.toList()));
                    if (!localRecommendedIds.isEmpty()) {
                        localUsersWithRecommendations = 1;
                    }
                }

                recommendedIds.addAll(localRecommendedIds);
                return localUsersWithRecommendations; // Zwracamy wynik
            } catch (Exception e) {
                logger.error("Error searching Lucene index: {}", e.getMessage(), e);
                System.out.println("Error searching Lucene index: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }, "ContentBasedFiltering", recommendedIds, groundTruth, totalUsers);

        logger.info("Content-Based Filtering Metrics: F1-Score: {}, Coverage: {}, Time: {} ms",
                metrics.f1Score, metrics.coverage, metrics.executionTime);
        System.out.printf("Content-Based Filtering Metrics: F1-Score: %.4f, Coverage: %.4f, Time: %d ms\n",
                metrics.f1Score, metrics.coverage, metrics.executionTime);

        return productDAO.findAllById(recommendedIds);
    }

    // Nowa metoda do dynamicznego generowania groundTruth za pomocą Lucene
    private Set<Integer> getDynamicGroundTruth(Long userId) {
        Set<Integer> baseGroundTruth = new HashSet<>();
        User user = userService.getCurrentUser();
        Cart cart = user.getCart();
        if (cart != null && cart.getItems() != null) {
            cart.getItems().forEach(item -> baseGroundTruth.add(item.getProduct().getId()));
        }
        List<Order> orders = entityManager.createQuery(
                        "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId", Order.class)
                .setParameter("userId", userId)
                .getResultList();
        orders.forEach(order -> order.getItems().forEach(item -> baseGroundTruth.add(item.getProduct().getId())));

        // Tworzymy indeks Lucene, aby znaleźć podobne produkty
        List<Product> allProducts = productDAO.findAll();
        if (allProducts.isEmpty()) {
            return baseGroundTruth; // Jeśli nie ma produktów, zwracamy tylko bazowy groundTruth
        }

        Map<Integer, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
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
        } catch (IOException e) {
            logger.error("Error creating Lucene index for groundTruth: {}", e.getMessage(), e);
            System.out.println("Error creating Lucene index for groundTruth: " + e.getMessage());
            return baseGroundTruth;
        }

        // Budujemy zapytanie na podstawie nazw i opisów produktów użytkownika
        StringBuilder queryStr = new StringBuilder();
        for (Integer productId : baseGroundTruth) {
            Product p = productMap.get(productId);
            if (p != null) {
                if (p.getName() != null) queryStr.append(p.getName()).append(" ");
                if (p.getDescription() != null) queryStr.append(p.getDescription()).append(" ");
            }
        }
        String queryString = queryStr.toString().trim();
        if (queryString.isEmpty()) {
            return baseGroundTruth;
        }

        // Rozszerzamy groundTruth o podobne produkty
        Set<Integer> extendedGroundTruth = new HashSet<>(baseGroundTruth);
        try (DirectoryReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            String[] fields = {"name", "description"};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, fields, analyzer);
            Query query = parser.parse(queryString);

            // Pobieramy więcej wyników, aby mieć większy zbiór groundTruth
            TopDocs topDocs = searcher.search(query, 10);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Integer productId = Integer.parseInt(doc.get("id"));
                // Dodajemy produkt do groundTruth, nawet jeśli jest już w baseGroundTruth (HashSet automatycznie unika duplikatów)
                extendedGroundTruth.add(productId);
            }
        } catch (Exception e) {
            logger.error("Error searching Lucene index for groundTruth: {}", e.getMessage(), e);
            System.out.println("Error searching Lucene index for groundTruth: " + e.getMessage());
            return baseGroundTruth;
        }

        logger.debug("Dynamic ground truth for userId {}: {}", userId, extendedGroundTruth);
        System.out.println("Dynamic ground truth for userId " + userId + ": " + extendedGroundTruth);
        return extendedGroundTruth;
    }

}