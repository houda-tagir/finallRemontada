package com.stackoverflow.analyzer.repository;

public final class HBaseTableNames {
    public static final String QUESTIONS_TABLE = "questions";
    public static final String ANSWERS_TABLE = "answers";
    public static final String TRENDS_TABLE = "com/stackOverFlow/analyzer";
    
    public static final String QUESTIONS_CF = "details";
    public static final String ANSWERS_CF = "details";
    public static final String TRENDS_CF = "stats";
    
    private HBaseTableNames() {
        // Private constructor to prevent instantiation
    }
}