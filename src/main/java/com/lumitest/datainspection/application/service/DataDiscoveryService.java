package com.lumitest.datainspection.application.service;

import com.lumitest.datainspection.domain.model.ComparisonResult;
import com.lumitest.datainspection.infrastructure.adapter.in.web.ConnectionParams;
import com.lumitest.util.discovery.DiscoveryResult;
import com.lumitest.util.discovery.MongoDataDiscovery;
import com.lumitest.util.discovery.PostgresDataDiscovery;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DataDiscoveryService {

    private final MongoDataDiscovery mongoDiscovery;
    private final PostgresDataDiscovery postgresDiscovery;

    public DataDiscoveryService(MongoTemplate mongoTemplate, JdbcTemplate jdbcTemplate) {
        this.mongoDiscovery = new MongoDataDiscovery(mongoTemplate);
        this.postgresDiscovery = new PostgresDataDiscovery(jdbcTemplate);
    }

    public Map<String, Optional<DiscoveryResult>> discoverAcrossAll(String fieldName) {
        Map<String, Optional<DiscoveryResult>> results = new HashMap<>();
        results.put("mongodb", mongoDiscovery.discover(fieldName));
        results.put("postgresql", postgresDiscovery.discover(fieldName));
        return results;
    }

    public Map<String, java.util.List<String>> getSchema() {
        Map<String, java.util.List<String>> schema = new HashMap<>();
        schema.put("mongodb", mongoDiscovery.listCollections());
        schema.put("postgresql", postgresDiscovery.listCollections());
        return schema;
    }

    public ComparisonResult compareFields(ConnectionParams sourceA, ConnectionParams sourceB, String fieldName) {
        Optional<DiscoveryResult> resultA = discoverForOne(sourceA, fieldName);
        Optional<DiscoveryResult> resultB = discoverForOne(sourceB, fieldName);

        boolean identical = resultA.isPresent() && resultB.isPresent() 
                && resultA.get().sampleValue().equals(resultB.get().sampleValue());

        return ComparisonResult.builder()
                .fieldName(fieldName)
                .sourceA(resultA.orElse(null))
                .sourceB(resultB.orElse(null))
                .identical(identical)
                .diffSummary(identical ? "Identical" : "Data mismatch or missing source")
                .build();
    }

    private Optional<DiscoveryResult> discoverForOne(ConnectionParams params, String fieldName) {
        if (params == null) return Optional.empty();
        
        // If it's the primary MongoDB from application.yml
        if ("MONGODB".equalsIgnoreCase(params.getDbType()) && params.getHost() == null) {
            return mongoDiscovery.discover(fieldName);
        }
        
        // If it's the primary PostgreSQL from application.yml
        if ("POSTGRESQL".equalsIgnoreCase(params.getDbType()) && params.getHost() == null) {
            return postgresDiscovery.discover(fieldName);
        }

        // TODO: Implement dynamic connection pooling for arbitrary hosts
        return Optional.empty();
    }
}
