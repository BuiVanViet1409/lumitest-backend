package com.lumitest.util.discovery;

import java.util.Optional;

/**
 * Interface for finding field locations and sample values across different databases.
 */
public interface DataDiscoveryTool {
    /**
     * Finds where a field exists and returns a sample value.
     * @param fieldName The name of the field to search for.
     * @return Optional containing DiscoveryResult if found, otherwise empty.
     */
    Optional<DiscoveryResult> discover(String fieldName);

    /**
     * Lists all available collections/tables in the database.
     * @return List of collection/table names.
     */
    java.util.List<String> listCollections();
}
