// src/main/java/com/stackoverflow/analyzer/model/StackOverflowQuestion.java
package com.stackoverflow.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StackOverflowQuestion {
    private String questionId;
    private String title;
    private long creationDate;
    private List<String> tags;
    private int viewCount;
    private int answerCount;
    private int score;
}