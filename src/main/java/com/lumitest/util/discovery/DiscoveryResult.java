package com.lumitest.util.discovery;

/**
 * Result of a data discovery operation.
 * @param location The collection/table name where the field was found.
 * @param sampleValue A sample non-null value for the field.
 */
public record DiscoveryResult(String location, Object sampleValue) {
}
