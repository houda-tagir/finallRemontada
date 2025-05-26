package com.stackoverflow.analyzer.service;

import com.stackoverflow.analyzer.model.StackOverflowQuestion;
import com.stackoverflow.analyzer.model.TagTrend;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StackOverflowService {

    /**
     * Get a question by its ID
     *
     * @param questionId The ID of the question
     * @return The question, or null if not found
     */
    StackOverflowQuestion getQuestion(String questionId) throws IOException;

    /**
     * Get trends for a specific tag over a time range
     *
     * @param tag The tag to get trends for
     * @param startTime The start of the time range
     * @param endTime The end of the time range
     * @return List of tag trends sorted by timestamp
     */
    List<TagTrend> getTagTrends(String tag, LocalDateTime startTime, LocalDateTime endTime) throws IOException;

    /**
     * Get the top tags by usage in the last 24 hours
     *
     * @param limit The maximum number of tags to return
     * @return List of tag names sorted by frequency
     */
    List<String> getTopTags(int limit) throws IOException;

    /**
     * Get trends for multiple tags over a time range
     *
     * @param tags The list of tags to get trends for
     * @param startTime The start of the time range
     * @param endTime The end of the time range
     * @return Map of tag names to their trend data
     */
    Map<String, List<TagTrend>> getMultiTagTrends(List<String> tags, LocalDateTime startTime, LocalDateTime endTime) throws IOException;

    /**
     * Get the distribution of tag usage in the last 24 hours
     *
     * @param limit The maximum number of tags to include
     * @return Map of tag names to their count
     */
    Map<String, Long> getTagDistribution(int limit) throws IOException;

    /**
     * Get hourly trend data for multiple tags over a specified number of hours
     *
     * @param tags The list of tags to get trends for
     * @param hours The number of hours to look back
     * @return Map of tag names to their hourly trend data
     */
    Map<String, List<TagTrend>> getHourlyTagTrends(List<String> tags, int hours) throws IOException;

    /**
     * Save a question to the repository
     *
     * @param question The question to save
     */
    void saveQuestion(StackOverflowQuestion question) throws IOException;
}