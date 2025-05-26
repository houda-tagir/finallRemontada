// src/main/java/com/stackoverflow/analyzer/config/HBaseInitializer.java
package com.stackoverflow.analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HBaseInitializer implements CommandLineRunner {

    private static final String QUESTIONS_TABLE = "stackoverflow_questions";
    private static final String TAGS_TABLE = "stackoverflow_tags";
    private static final String QUESTION_CF = "question";
    private static final String TAG_CF = "tag";
    private static final String TREND_CF = "trend";

    private final Connection hbaseConnection;

    @Override
    public void run(String... args) throws Exception {
        initHBaseTables();
    }

    private void initHBaseTables() {
        try (Admin admin = hbaseConnection.getAdmin()) {
            // Create questions table if it doesn't exist
            if (!admin.tableExists(TableName.valueOf(QUESTIONS_TABLE))) {
                log.info("Creating HBase table: {}", QUESTIONS_TABLE);
                HTableDescriptor questionsTable = new HTableDescriptor(TableName.valueOf(QUESTIONS_TABLE));
                questionsTable.addFamily(new HColumnDescriptor(QUESTION_CF));
                admin.createTable(questionsTable);
                log.info("Created HBase table: {}", QUESTIONS_TABLE);
            } else {
                log.info("HBase table already exists: {}", QUESTIONS_TABLE);
            }

            // Create tags table if it doesn't exist
            if (!admin.tableExists(TableName.valueOf(TAGS_TABLE))) {
                log.info("Creating HBase table: {}", TAGS_TABLE);
                HTableDescriptor tagsTable = new HTableDescriptor(TableName.valueOf(TAGS_TABLE));
                tagsTable.addFamily(new HColumnDescriptor(TAG_CF));
                tagsTable.addFamily(new HColumnDescriptor(TREND_CF));
                admin.createTable(tagsTable);
                log.info("Created HBase table: {}", TAGS_TABLE);
            } else {
                log.info("HBase table already exists: {}", TAGS_TABLE);
            }
        } catch (IOException e) {
            log.error("Error initializing HBase tables", e);
        }
    }
}