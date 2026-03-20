package com.lumitest.testutils;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of DataDiscoveryTool.
 * Queries information_schema.columns to find the table, then fetches a sample non-null value.
 */
public class PostgresDataDiscovery implements DataDiscoveryTool {

    private final JdbcTemplate jdbcTemplate;

    public PostgresDataDiscovery(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<DiscoveryResult> discover(String fieldName) {
        // Find tables containing the column
        String findTableSql = "SELECT table_name FROM information_schema.columns " +
                             "WHERE column_name = ? AND table_schema = 'public' LIMIT 1";
        
        List<String> tables = jdbcTemplate.queryForList(findTableSql, String.class, fieldName);
        
        if (tables.isEmpty()) {
            return Optional.empty();
        }

        String tableName = tables.get(0);
        
        // Fetch a sample non-null value
        String sampleValueSql = String.format("SELECT %s FROM %s WHERE %s IS NOT NULL LIMIT 1", 
                                             fieldName, tableName, fieldName);
        
        try {
            Object sampleValue = jdbcTemplate.queryForObject(sampleValueSql, Object.class);
            return Optional.of(new DiscoveryResult(tableName, sampleValue));
        } catch (Exception e) {
            // Field might be null or table empty
            return Optional.empty();
        }
    }
}
