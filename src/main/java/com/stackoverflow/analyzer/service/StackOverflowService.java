// src/main/java/com/stackoverflow/analyzer/service/StackOverflowService.java
package com.stackoverflow.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackoverflow.analyzer.model.StackOverflowQuestion;
import com.stackoverflow.analyzer.model.TagTrend;
import com.stackoverflow.analyzer.repository.HBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StackOverflowService {

    private final HBaseRepository hbaseRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Cache to avoid fetching the same data repeatedly
    private final Map<String, List<TagTrend>> trendCache = new ConcurrentHashMap<>();

    @Value("${stackexchange.api.url}")
    private String apiUrl;

    @Value("${stackexchange.api.site}")
    private String site;

    @Value("${stackexchange.api.pagesize}")
    private int pageSize;

    /**
     * Scheduled task to fetch new questions from StackExchange API
     * and update HBase
     */
    @Scheduled(fixedDelayString = "${stackexchange.api.interval}")
    public void fetchAndStoreQuestions() {
        try {
            log.info("Fetching new questions from Stack Exchange API");
            String url = String.format("%s?order=desc&sort=creation&site=%s&pagesize=%d",
                    apiUrl, site, pageSize);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            List<StackOverflowQuestion> questions = new ArrayList<>();

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    String questionId = item.get("question_id").asText();
                    String title = item.get("title").asText();
                    long creationDate = item.get("creation_date").asLong();
                    int viewCount = item.get("view_count").asInt();
                    int answerCount = item.get("answer_count").asInt();
                    int score = item.get("score").asInt();

                    List<String> tags = new ArrayList<>();
                    JsonNode tagsNode = item.get("tags");
                    if (tagsNode != null && tagsNode.isArray()) {
                        for (JsonNode tag : tagsNode) {
                            tags.add(tag.asText());
                        }
                    }

                    StackOverflowQuestion question = StackOverflowQuestion.builder()
                            .questionId(questionId)
                            .title(title)
                            .creationDate(creationDate)
                            .tags(tags)
                            .viewCount(viewCount)
                            .answerCount(answerCount)
                            .score(score)
                            .build();

                    questions.add(question);
                    hbaseRepository.saveQuestion(question);
                }
            }

            log.info("Stored {} new questions in HBase", questions.size());

            // Clear the cache to ensure fresh data
            trendCache.clear();

            // Send real-time update via WebSocket
            updateTrendData();

        } catch (Exception e) {
            log.error("Error fetching and storing questions", e);
        }
    }

    /**
     * Get trend data for a specific tag
     */
    public TagTrend getTagTrend(String tag, int days) {
        long endTime = Instant.now().getEpochSecond();
        long startTime = Instant.now().minus(days, ChronoUnit.DAYS).getEpochSecond();

        return hbaseRepository.getTagTrend(tag, startTime, endTime);
    }

    /**
     * Get questions with a specific tag
     */
    public List<StackOverflowQuestion> getQuestionsByTag(String tag) {
        return hbaseRepository.getQuestionsByTag(tag);
    }

    /**
     * Get the top N tags
     */
    public List<String> getTopTags(int limit) {
        return hbaseRepository.getTopTags(limit);
    }

    /**
     * Send trend updates to the WebSocket clients
     */
    private void updateTrendData() {
        List<String> topTags = getTopTags(10);
        List<TagTrend> trends = topTags.stream()
                .map(tag -> getTagTrend(tag, 7))
                .collect(Collectors.toList());

        // Send update to clients
        messagingTemplate.convertAndSend("/topic/trends", trends);
    }
}