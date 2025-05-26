package com.stackoverflow.analyzer.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class HBaseConfig {

    @Value("${hbase.zookeeper.quorum}")
    private String zookeeperQuorum;

    @Value("${hbase.zookeeper.property.clientPort}")
    private String zookeeperClientPort;

    @Value("${hbase.zookeeper.znode.parent}")
    private String zookeeperZnodeParent;

    /**
     * Create and configure HBase configuration
     *
     * @return HBase configuration
     */
    @Bean
    public Configuration hbaseConfiguration() {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zookeeperQuorum);
        configuration.set("hbase.zookeeper.property.clientPort", zookeeperClientPort);
        configuration.set("hbase.zookeeper.znode.parent", zookeeperZnodeParent);
        return configuration;
    }

    /**
     * Create HBase connection
     *
     * @param configuration HBase configuration
     * @return HBase connection
     * @throws IOException if connection creation fails
     */
    @Bean
    public Connection hbaseConnection(Configuration configuration) throws IOException {
        return ConnectionFactory.createConnection(configuration);
    }
}