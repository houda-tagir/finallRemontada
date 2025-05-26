package com.stackoverflow.analyzer.controller;

import com.stackoverflow.analyzer.model.StackOverflowQuestion;
import com.stackoverflow.analyzer.model.TagTrend;
import com.stackoverflow.analyzer.service.StackOverflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StackOverflowController {

    private final StackOverflowService stackOverflowService;

    @Autowired
    public StackOverflowController(StackOverflowService stackOverflowService) {
        this.stackOverflowService = stackOverflowService;
    }

    /**
     * Get a question by ID
     */
    @GetMapping("/questions/{id}")
    public ResponseEntity<StackOverflowQuestion> getQuestion(@PathVariable String id) {
        try {
            StackOverflowQuestion question = stackOverflowService.getQuestion(id);
            if (question == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(question);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get trends for a specific tag
     */
    @GetMapping("/trends/{tag}")
    public ResponseEntity<List<TagTrend>> getTagTrends(
            @PathVariable String tag,
            @RequestParam(defaultValue = "24") int hours) {

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minus(hours, ChronoUnit.HOURS);

            List<TagTrend> trends = stackOverflowService.getTagTrends(tag, startTime, endTime);
            return ResponseEntity.ok(trends);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top tags
     */
    @GetMapping("/tags/top")
    public ResponseEntity<List<String>> getTopTags(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> topTags = stackOverflowService.getTopTags(limit);
            return ResponseEntity.ok(topTags);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get tag distribution
     */
    @GetMapping("/tags/distribution")
    public ResponseEntity<Map<String, Long>> getTagDistribution(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            Map<String, Long> distribution = stackOverflowService.getTagDistribution(limit);
            return ResponseEntity.ok(distribution);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get multi-tag trends
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, List<TagTrend>>> getMultiTagTrends(
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "24") int hours) {

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minus(hours, ChronoUnit.HOURS);

            Map<String, List<TagTrend>> trends = stackOverflowService.getMultiTagTrends(tags, startTime, endTime);
            return ResponseEntity.ok(trends);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get hourly tag trends (for charts)
     */
    @GetMapping("/trends/hourly")
    public ResponseEntity<Map<String, List<TagTrend>>> getHourlyTagTrends(
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "24") int hours) {

        try {
            Map<String, List<TagTrend>> trends = stackOverflowService.getHourlyTagTrends(tags, hours);
            return ResponseEntity.ok(trends);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * WebSocket endpoint for getting real-time tag trends
     */
    @MessageMapping("/getTrends")
    @SendTo("/topic/trends")
    public Map<String, List<TagTrend>> getTrends(TrendRequest request) throws IOException {
        List<String> tags = request.getTags();
        int hours = request.getHours();

        if (tags == null || tags.isEmpty()) {
            tags = stackOverflowService.getTopTags(5);
        }

        if (hours <= 0) {
            hours = 24;
        }

        return stackOverflowService.getHourlyTagTrends(tags, hours);
    }

    /**
     * Request class for WebSocket trend requests
     */
    public static class TrendRequest {
        private List<String> tags;
        private int hours = 24;

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public int getHours() {
            return hours;
        }

        public void setHours(int hours) {
            this.hours = hours;
        }
    }
}