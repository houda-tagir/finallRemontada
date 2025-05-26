package com.stackoverflow.analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.annotation.PreDestroy;
import java.io.IOException;

@org.springframework.context.annotation.Configuration
@Slf4j
public class HBaseConfig {

    private Connection connection;

    @Value("${hbase.zookeeper.quorum}")
    private String zookeeperQuorum;

    @Value("${hbase.zookeeper.property.clientPort}")
    private String zookeeperClientPort;

    @Value("${hbase.zookeeper.znode.parent}")
    private String zookeeperZnodeParent;

    @Bean
    @Primary
    public Configuration hbaseConfiguration() {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zookeeperQuorum);
        configuration.set("hbase.zookeeper.property.clientPort", zookeeperClientPort);
        configuration.set("hbase.zookeeper.znode.parent", zookeeperZnodeParent);

        // Increase timeouts for connection
        configuration.set("hbase.client.retries.number", "7");
        configuration.set("hbase.client.pause", "1000");
        configuration.set("zookeeper.recovery.retry", "7");
        configuration.set("zookeeper.recovery.retry.intervalmill", "1000");

        return configuration;
    }

    @Bean
    public Connection hbaseConnection(Configuration configuration) throws IOException {
        if (connection == null || connection.isClosed()) {
            try {
                log.info("Creating new HBase connection to {}", zookeeperQuorum);
                connection = ConnectionFactory.createConnection(configuration);
                log.info("HBase connection established successfully");
            } catch (IOException e) {
                log.error("Failed to create HBase connection", e);
                throw e;
            }
        }
        return connection;
    }

    @PreDestroy
    public void closeConnection() {
        if (connection != null) {
            try {
                log.info("Closing HBase connection");
                connection.close();
            } catch (IOException e) {
                log.error("Error closing HBase connection", e);
            }
        }
    }
}