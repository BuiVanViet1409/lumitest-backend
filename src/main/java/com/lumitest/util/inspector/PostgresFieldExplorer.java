package com.lumitest.util.inspector;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL implementation of FieldExplorer.
 * Queries information_schema for efficiency and queries specific tables for sample values.
 */
public class PostgresFieldExplorer implements FieldExplorer {

    private final JdbcTemplate jdbcTemplate;

    public PostgresFieldExplorer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DiscoveryResult> explore(String fieldName) {
        List<DiscoveryResult> results = new ArrayList<>();

        // 1. Find all tables containing the column
        String findTablesSql = "SELECT table_name FROM information_schema.columns " +
                              "WHERE column_name = ? AND table_schema = 'public'";
        
        List<String> tableNames = jdbcTemplate.queryForList(findTablesSql, String.class, fieldName);

        // 2. Query each table for a sample value (Safe query with LIMIT 1)
        for (String tableName : tableNames) {
            String sampleValueSql = String.format("SELECT %s FROM %s WHERE %s IS NOT NULL LIMIT 1", 
                                                 fieldName, tableName, fieldName);
            try {
                Object value = jdbcTemplate.queryForObject(sampleValueSql, Object.class);
                if (value != null) {
                    results.add(new DiscoveryResult("PostgreSQL", tableName, value));
                }
            } catch (Exception e) {
                // Table might be empty or other transient error
            }
        }

        return results;
    }
}
