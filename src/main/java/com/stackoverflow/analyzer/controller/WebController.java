// src/main/java/com/stackoverflow/analyzer/controller/WebController.java
package com.stackoverflow.analyzer.controller;

import com.stackoverflow.analyzer.service.StackOverflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final StackOverflowService stackOverflowService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("topTags", stackOverflowService.getTopTags(10));
        return "index";
    }

    @GetMapping("/tag/{tag}")
    public String tagDetails(@PathVariable String tag,
                             @RequestParam(defaultValue = "7") int days,
                             Model model) {
        model.addAttribute("tag", tag);
        model.addAttribute("days", days);
        model.addAttribute("questions", stackOverflowService.getQuestionsByTag(tag));
        model.addAttribute("trend", stackOverflowService.getTagTrend(tag, days));
        return "tag-details";
    }
}