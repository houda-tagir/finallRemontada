// src/main/java/com/stackoverflow/analyzer/controller/StackOverflowController.java
package com.stackoverflow.analyzer.controller;

import com.stackoverflow.analyzer.model.StackOverflowQuestion;
import com.stackoverflow.analyzer.model.TagTrend;
import com.stackoverflow.analyzer.service.StackOverflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StackOverflowController {

    private final StackOverflowService stackOverflowService;

    @GetMapping("/tags/top")
    public ResponseEntity<List<String>> getTopTags(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(stackOverflowService.getTopTags(limit));
    }

    @GetMapping("/tags/{tag}/trend")
    public ResponseEntity<TagTrend> getTagTrend(
            @PathVariable String tag,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(stackOverflowService.getTagTrend(tag, days));
    }

    @GetMapping("/tags/{tag}/questions")
    public ResponseEntity<List<StackOverflowQuestion>> getQuestionsByTag(@PathVariable String tag) {
        return ResponseEntity.ok(stackOverflowService.getQuestionsByTag(tag));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> manualRefresh() {
        stackOverflowService.fetchAndStoreQuestions();
        return ResponseEntity.ok().build();
    }
}