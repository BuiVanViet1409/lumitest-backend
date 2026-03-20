package com.lumitest.util.inspector;

import java.util.List;

/**
 * Interface for exploring configuration fields in a database.
 */
public interface FieldExplorer {
    /**
     * Finds the specified field and returns a list of results.
     * @param fieldName The name of the field to inspect.
     * @return A list of matching locations and values.
     */
    List<DiscoveryResult> explore(String fieldName);
}
