package com.example.springlogowanie;

import com.example.springlogowanie.service.TestDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class SpringLogowanieApplication implements CommandLineRunner {

    @Autowired
    private TestDataGenerator testDataGenerator;
    @Override
    public void run(String... args) throws Exception {

        testDataGenerator.generateTestData();
        System.out.println("Test data generated successfully!");
    }
    public static void main(String[] args) {
        SpringApplication.run(SpringLogowanieApplication.class, args);
    }


}