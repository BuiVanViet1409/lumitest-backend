package com.lumitest.testutils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example usage of DataDiscoveryTool in a JUnit test.
 * This is a SpringBootTest to demonstrate integration with Spring-managed beans.
 */
@SpringBootTest
public class DataDiscoveryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testMongoDiscovery() {
        DataDiscoveryTool mongoTool = new MongoDataDiscovery(mongoTemplate);
        String fieldToSearch = "status"; // Replace with actual field
        
        Optional<DiscoveryResult> result = mongoTool.discover(fieldToSearch);
        
        result.ifPresentOrElse(
            res -> System.out.printf("Found field '%s' in MongoDB collection '%s' with sample value: %s%n", 
                                     fieldToSearch, res.location(), res.sampleValue()),
            () -> System.out.printf("Field '%s' not found in any MongoDB collection.%n", fieldToSearch)
        );
    }

    @Test
    void testPostgresDiscovery() {
        DataDiscoveryTool postgresTool = new PostgresDataDiscovery(jdbcTemplate);
        String fieldToSearch = "email"; // Replace with actual field
        
        Optional<DiscoveryResult> result = postgresTool.discover(fieldToSearch);
        
        result.ifPresentOrElse(
            res -> System.out.printf("Found field '%s' in PostgreSQL table '%s' with sample value: %s%n", 
                                     fieldToSearch, res.location(), res.sampleValue()),
            () -> System.out.printf("Field '%s' not found in any PostgreSQL table.%n", fieldToSearch)
        );
    }
}
