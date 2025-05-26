package com.stackoverflow.analyzer.service;

import com.stackoverflow.analyzer.model.StackOverflowQuestion;
import com.stackoverflow.analyzer.model.TagTrend;
import com.stackoverflow.analyzer.repository.HBaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StackOverflowServiceImpl implements StackOverflowService {

    private final HBaseRepository hBaseRepository;

    @Autowired
    public StackOverflowServiceImpl(HBaseRepository hBaseRepository) {
        this.hBaseRepository = hBaseRepository;
    }

    @Override
    public StackOverflowQuestion getQuestion(String questionId) throws IOException {
        // Implementation pending
        return null;
    }

    @Override
    public List<TagTrend> getTagTrends(String tag, LocalDateTime startTime, LocalDateTime endTime) throws IOException {
        long startTimestamp = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        TagTrend tagTrend = hBaseRepository.getTagTrend(tag, startTimestamp, endTimestamp);
        List<TagTrend> result = new ArrayList<>();
        result.add(tagTrend);
        return result;
    }

    @Override
    public List<String> getTopTags(int limit) throws IOException {
        return hBaseRepository.getTopTags(limit);
    }

    @Override
    public Map<String, List<TagTrend>> getMultiTagTrends(List<String> tags, LocalDateTime startTime, LocalDateTime endTime) throws IOException {
        Map<String, List<TagTrend>> result = new HashMap<>();
        for (String tag : tags) {
            result.put(tag, getTagTrends(tag, startTime, endTime));
        }
        return result;
    }

    @Override
    public Map<String, Long> getTagDistribution(int limit) throws IOException {
        // Implementation pending
        return new HashMap<>();
    }

    @Override
    public Map<String, List<TagTrend>> getHourlyTagTrends(List<String> tags, int hours) throws IOException {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);
        return getMultiTagTrends(tags, startTime, endTime);
    }

    @Override
    public void saveQuestion(StackOverflowQuestion question) throws IOException {
        hBaseRepository.saveQuestion(question);
    }
}