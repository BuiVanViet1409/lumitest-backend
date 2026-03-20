package com.lumitest.util.inspector;

/**
 * Result of a configuration field inspection.
 */
public record DiscoveryResult(
    String database,
    String location,
    Object sampleValue,
    boolean isMismatch
) {
    public DiscoveryResult(String database, String location, Object sampleValue) {
        this(database, location, sampleValue, false);
    }
    
    public DiscoveryResult withMismatch(boolean mismatch) {
        return new DiscoveryResult(database, location, sampleValue, mismatch);
    }
}
