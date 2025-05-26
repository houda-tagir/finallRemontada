
// src/main/java/com/stackoverflow/analyzer/repository/HBaseRepository.java
package com.stackoverflow.analyzer.repository;

import com.stackoverflow.analyzer.model.StackOverflowQuestion;
import com.stackoverflow.analyzer.model.TagTrend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class HBaseRepository {

    private static final String QUESTIONS_TABLE = "stackoverflow_questions";
    private static final String TAGS_TABLE = "stackoverflow_tags";
    private static final String QUESTION_CF = "question";
    private static final String TAG_CF = "tag";
    private static final String TREND_CF = "trend";

    private final Connection hbaseConnection;

    public void saveQuestion(StackOverflowQuestion question) {
        try {
            Table questionsTable = hbaseConnection.getTable(TableName.valueOf(QUESTIONS_TABLE));

            Put put = new Put(Bytes.toBytes(question.getQuestionId()));
            put.addColumn(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("title"), Bytes.toBytes(question.getTitle()));
            put.addColumn(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("creation_date"), Bytes.toBytes(question.getCreationDate()));
            put.addColumn(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("tags"),
                    Bytes.toBytes(String.join(",", question.getTags())));
            put.addColumn(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("view_count"), Bytes.toBytes(question.getViewCount()));
            put.addColumn(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("answer_count"), Bytes.toBytes(question.getAnswerCount()));
            put.addColumn(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("score"), Bytes.toBytes(question.getScore()));

            questionsTable.put(put);
            questionsTable.close();

            // Also update the tags table for each tag
            updateTagsTable(question);

        } catch (IOException e) {
            log.error("Error saving question to HBase", e);
        }
    }

    private void updateTagsTable(StackOverflowQuestion question) throws IOException {
        Table tagsTable = hbaseConnection.getTable(TableName.valueOf(TAGS_TABLE));

        long timestamp = question.getCreationDate();
        String timeKey = String.valueOf(timestamp - (timestamp % 3600)); // Hourly granularity

        for (String tag : question.getTags()) {
            // Increment the count for this tag at this time
            Put put = new Put(Bytes.toBytes(tag));

            // Update the overall tag count
            put.addColumn(
                    Bytes.toBytes(TAG_CF),
                    Bytes.toBytes("count"),
                    Bytes.toBytes(1)
            );

            // Update the trend data for this hour
            put.addColumn(
                    Bytes.toBytes(TREND_CF),
                    Bytes.toBytes(timeKey),
                    Bytes.toBytes(1)
            );

            tagsTable.put(put);
        }

        tagsTable.close();
    }

    public List<StackOverflowQuestion> getQuestionsByTag(String tag) {
        List<StackOverflowQuestion> questions = new ArrayList<>();

        try {
            Table questionsTable = hbaseConnection.getTable(TableName.valueOf(QUESTIONS_TABLE));

            // Create a filter to find questions with the specific tag
            SingleColumnValueFilter filter = new SingleColumnValueFilter(
                    Bytes.toBytes(QUESTION_CF),
                    Bytes.toBytes("tags"),
                    CompareOp.EQUAL,
                    new SubstringComparator(tag)
            );

            Scan scan = new Scan();
            scan.setFilter(filter);

            ResultScanner scanner = questionsTable.getScanner(scan);
            for (Result result : scanner) {
                StackOverflowQuestion question = mapResultToQuestion(result);
                questions.add(question);
            }

            scanner.close();
            questionsTable.close();

        } catch (IOException e) {
            log.error("Error getting questions by tag from HBase", e);
        }

        return questions;
    }

    public TagTrend getTagTrend(String tag, long startTime, long endTime) {
        TagTrend tagTrend = new TagTrend();
        tagTrend.setTagName(tag);
        List<TagTrend.TimePoint> timePoints = new ArrayList<>();

        try {
            Table tagsTable = hbaseConnection.getTable(TableName.valueOf(TAGS_TABLE));

            Get get = new Get(Bytes.toBytes(tag));
            Result result = tagsTable.get(get);

            if (!result.isEmpty()) {
                // Process all trend columns (timestamps)
                NavigableMap<byte[], byte[]> trendColumns = result.getFamilyMap(Bytes.toBytes(TREND_CF));

                for (Map.Entry<byte[], byte[]> entry : trendColumns.entrySet()) {
                    String timeKey = Bytes.toString(entry.getKey());
                    long timestamp = Long.parseLong(timeKey);

                    // Only include timestamps within the requested range
                    if (timestamp >= startTime && timestamp <= endTime) {
                        int count = Bytes.toInt(entry.getValue());
                        timePoints.add(new TagTrend.TimePoint(timestamp, count));
                    }
                }
            }

            tagsTable.close();

        } catch (IOException e) {
            log.error("Error getting tag trend from HBase", e);
        }

        // Sort time points by timestamp
        timePoints.sort(Comparator.comparing(TagTrend.TimePoint::getTimestamp));
        tagTrend.setTimePoints(timePoints);

        return tagTrend;
    }

    public List<String> getTopTags(int limit) {
        List<Map.Entry<String, Integer>> tagCounts = new ArrayList<>();

        try {
            Table tagsTable = hbaseConnection.getTable(TableName.valueOf(TAGS_TABLE));

            Scan scan = new Scan();
            scan.addFamily(Bytes.toBytes(TAG_CF));

            ResultScanner scanner = tagsTable.getScanner(scan);
            for (Result result : scanner) {
                String tag = Bytes.toString(result.getRow());
                byte[] countBytes = result.getValue(Bytes.toBytes(TAG_CF), Bytes.toBytes("count"));

                if (countBytes != null) {
                    int count = Bytes.toInt(countBytes);
                    tagCounts.add(new AbstractMap.SimpleEntry<>(tag, count));
                }
            }

            scanner.close();
            tagsTable.close();

        } catch (IOException e) {
            log.error("Error getting top tags from HBase", e);
        }

        // Sort by count in descending order and take the top 'limit' tags
        return tagCounts.stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private StackOverflowQuestion mapResultToQuestion(Result result) {
        String questionId = Bytes.toString(result.getRow());
        String title = Bytes.toString(result.getValue(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("title")));
        long creationDate = Bytes.toLong(result.getValue(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("creation_date")));
        String tagsStr = Bytes.toString(result.getValue(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("tags")));
        List<String> tags = Arrays.asList(tagsStr.split(","));
        int viewCount = Bytes.toInt(result.getValue(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("view_count")));
        int answerCount = Bytes.toInt(result.getValue(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("answer_count")));
        int score = Bytes.toInt(result.getValue(Bytes.toBytes(QUESTION_CF), Bytes.toBytes("score")));

        return StackOverflowQuestion.builder()
                .questionId(questionId)
                .title(title)
                .creationDate(creationDate)
                .tags(tags)
                .viewCount(viewCount)
                .answerCount(answerCount)
                .score(score)
                .build();
    }
    public void createTableIfNotExists(String tableName, String columnFamily) throws IOException {
        try (Admin admin = hbaseConnection.getAdmin()) {
            TableName table = TableName.valueOf(tableName);
            if (!admin.tableExists(table)) {
                log.info("Creating HBase table: {}", tableName);
                HTableDescriptor tableDescriptor = new HTableDescriptor(table);
                tableDescriptor.addFamily(new HColumnDescriptor(columnFamily));
                admin.createTable(tableDescriptor);
                log.info("Created HBase table: {}", tableName);
            } else {
                log.info("HBase table already exists: {}", tableName);
            }
        }
    }
}
