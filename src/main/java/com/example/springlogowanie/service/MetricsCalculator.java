package com.example.springlogowanie.service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class MetricsCalculator {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Klasa do przechowywania wyników metryk
    public static class Metrics {
        public double f1Score; // F1-score
        public double coverage; // Pokrycie
        public long executionTime; // Czas wykonania w ms

        public Metrics(double f1Score, double coverage, long executionTime) {
            this.f1Score = f1Score;
            this.coverage = coverage;
            this.executionTime = executionTime;
        }
    }

    // Obliczanie F1-score
    public static double calculateF1Score(List<Integer> recommendations, Set<Integer> groundTruth) {
        if (recommendations.isEmpty() || groundTruth.isEmpty()) {
            return 0.0;
        }

        int tp = 0; // True Positives
        for (Integer rec : recommendations) {
            if (groundTruth.contains(rec)) {
                tp++;
            }
        }

        double precision = tp / (double) recommendations.size(); // TP / (TP + FP)
        double recall = tp / (double) groundTruth.size(); // TP / (TP + FN)
        if (precision + recall == 0) {
            return 0.0;
        }
        return 2 * (precision * recall) / (precision + recall);
    }

    // Obliczanie pokrycia
    public static double calculateCoverage(int usersWithRecommendations, int totalUsers) {
        return totalUsers > 0 ? (double) usersWithRecommendations / totalUsers : 0.0;
    }

    // Pomiar metryk dla algorytmu
    public static Metrics measureMetrics(java.util.function.IntSupplier algorithm, String algorithmName,
                                         List<Integer> recommendations, Set<Integer> groundTruth,
                                         int totalUsers) {
        long startTime = System.currentTimeMillis();
        int usersWithRecommendations = algorithm.getAsInt(); // Wykonujemy lambdę i pobieramy wynik
        long executionTime = System.currentTimeMillis() - startTime;

        double f1Score = calculateF1Score(recommendations, groundTruth);
        double coverage = calculateCoverage(usersWithRecommendations, totalUsers);

        logMetrics(algorithmName, f1Score, coverage, executionTime);

        return new Metrics(f1Score, coverage, executionTime);
    }

    // Logowanie wyników do pliku CSV
    public static void logMetrics(String algorithmName, double f1Score, double coverage, long executionTime) {
        try (FileWriter writer = new FileWriter("src/main/resources/recommendation_metrics.csv", true)) {
            String timestamp = LocalDateTime.now().format(formatter);
            String log = String.format("%s,%s,%.4f,%.4f,%d\n",
                    timestamp, algorithmName, f1Score, coverage, executionTime);
            writer.write(log);
        } catch (IOException e) {
            System.err.println("Błąd zapisu do pliku: " + e.getMessage());
        }
    }

    // Inicjalizacja pliku CSV
    public static void initializeLogFile() {
        try (FileWriter writer = new FileWriter("src/main/resources/recommendation_metrics.csv")) {
            writer.write("Timestamp,Algorithm,F1_Score,Coverage,Execution_Time (ms)\n");
        } catch (IOException e) {
            System.err.println("Błąd inicjalizacji pliku CSV: " + e.getMessage());
        }
    }
}