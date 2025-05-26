// src/main/java/com/stackoverflow/analyzer/model/TagTrend.java
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
public class TagTrend {
    private String tagName;
    private List<TimePoint> timePoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePoint {
        private long timestamp;
        private int count;
    }
}