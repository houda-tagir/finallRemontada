package com.stackoverflow.analyzer.service;

import com.stackoverflow.analyzer.repository.HBaseRepository;
import com.stackoverflow.analyzer.repository.HBaseTableNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HBaseInitializationService {

    private final HBaseRepository hbaseRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeHBaseTables() {
        try {
            // Create Questions table
            hbaseRepository.createTableIfNotExists(
                HBaseTableNames.QUESTIONS_TABLE,
                HBaseTableNames.QUESTIONS_CF
            );
            
            // Create Answers table
            hbaseRepository.createTableIfNotExists(
                HBaseTableNames.ANSWERS_TABLE,
                HBaseTableNames.ANSWERS_CF
            );
            
            // Create Trends table
            hbaseRepository.createTableIfNotExists(
                HBaseTableNames.TRENDS_TABLE,
                HBaseTableNames.TRENDS_CF
            );
            
            log.info("HBase tables initialized successfully");
        } catch (IOException e) {
            log.error("Error initializing HBase tables: {}", e.getMessage(), e);
        }
    }
}