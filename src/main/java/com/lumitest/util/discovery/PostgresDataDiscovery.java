package com.lumitest.util.discovery;

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
        String findTableSql = "SELECT table_name FROM information_schema.columns " +
                             "WHERE column_name = ? AND table_schema = 'public' LIMIT 1";
        
        List<String> tables = jdbcTemplate.queryForList(findTableSql, String.class, fieldName);
        
        if (tables.isEmpty()) {
            return Optional.empty();
        }

        String tableName = tables.get(0);
        String sampleValueSql = String.format("SELECT %s FROM %s WHERE %s IS NOT NULL LIMIT 1", 
                                             fieldName, tableName, fieldName);
        
        try {
            Object sampleValue = jdbcTemplate.queryForObject(sampleValueSql, Object.class);
            return Optional.of(new DiscoveryResult(tableName, sampleValue));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public java.util.List<String> listCollections() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
