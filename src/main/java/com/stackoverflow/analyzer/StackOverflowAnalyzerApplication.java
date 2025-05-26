// src/main/java/com/stackoverflow/analyzer/StackOverflowAnalyzerApplication.java
package com.stackoverflow.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Enable scheduled tasks for periodic data fetching
public class StackOverflowAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StackOverflowAnalyzerApplication.class, args);
    }
}