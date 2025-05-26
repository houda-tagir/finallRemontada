package com.stackoverflow.analyzer.controller;

import com.stackoverflow.analyzer.model.TagTrend;
import com.stackoverflow.analyzer.service.StackOverflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final StackOverflowService stackOverflowService;

    @Autowired
    public WebController(StackOverflowService stackOverflowService) {
        this.stackOverflowService = stackOverflowService;
    }

    /**
     * Home page controller - shows dashboard with real-time tag trends
     */
    @GetMapping("/")
    public String home(Model model) {
        try {
            // Get top 5 tags for the dashboard
            List<String> topTags = stackOverflowService.getTopTags(5);
            model.addAttribute("topTags", topTags);

            // Get initial data for tag distribution chart
            Map<String, Long> tagDistribution = stackOverflowService.getTagDistribution(10);
            model.addAttribute("tagDistribution", tagDistribution);

            // Get initial data for trend charts (last 24 hours)
            if (!topTags.isEmpty()) {
                Map<String, List<TagTrend>> trends = stackOverflowService.getHourlyTagTrends(topTags, 24);
                model.addAttribute("tagTrends", trends);
            }

        } catch (IOException e) {
            model.addAttribute("error", "Error loading data: " + e.getMessage());
        }

        return "index";
    }

    /**
     * Tags page controller - shows detailed tag trends
     */
    @GetMapping("/tags")
    public String tags(@RequestParam(required = false) List<String> selectedTags,
                       @RequestParam(defaultValue = "24") int hours,
                       Model model) {
        try {
            // If no tags selected, get top 10
            List<String> tags = selectedTags;
            if (tags == null || tags.isEmpty()) {
                tags = stackOverflowService.getTopTags(10);
            }

            model.addAttribute("allTags", stackOverflowService.getTopTags(50));
            model.addAttribute("selectedTags", tags);
            model.addAttribute("hours", hours);

            // Get hourly trends for selected tags
            Map<String, List<TagTrend>> trends = stackOverflowService.getHourlyTagTrends(tags, hours);
            model.addAttribute("tagTrends", trends);

        } catch (IOException e) {
            model.addAttribute("error", "Error loading data: " + e.getMessage());
        }

        return "tags";
    }

    /**
     * Comparison page controller - for comparing multiple tags
     */
    @GetMapping("/compare")
    public String compare(@RequestParam(required = false) List<String> tags,
                          @RequestParam(defaultValue = "7") int days,
                          Model model) {
        try {
            // If no tags selected, get top 5
            List<String> selectedTags = tags;
            if (selectedTags == null || selectedTags.isEmpty()) {
                selectedTags = stackOverflowService.getTopTags(5);
            }

            model.addAttribute("allTags", stackOverflowService.getTopTags(50));
            model.addAttribute("selectedTags", selectedTags);
            model.addAttribute("days", days);

            // Get data for the selected time period
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minus(days, ChronoUnit.DAYS);

            Map<String, List<TagTrend>> trends = stackOverflowService.getMultiTagTrends(selectedTags, startTime, endTime);
            model.addAttribute("tagTrends", trends);

        } catch (IOException e) {
            model.addAttribute("error", "Error loading data: " + e.getMessage());
        }

        return "compare";
    }
}