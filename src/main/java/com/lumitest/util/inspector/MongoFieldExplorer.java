package com.lumitest.util.inspector;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * MongoDB implementation of FieldExplorer.
 * Uses a whitelist to search only specific config collections for safety.
 */
public class MongoFieldExplorer implements FieldExplorer {

    private final MongoTemplate mongoTemplate;
    private final Set<String> whitelist = Set.of(
        "app_config", "system_settings", "environment_params", 
        "feature_flags", "tenant_config", "global_vars", "test_cases"
    );

    public MongoFieldExplorer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<DiscoveryResult> explore(String fieldName) {
        List<DiscoveryResult> results = new ArrayList<>();
        MongoDatabase db = mongoTemplate.getDb();

        for (String collectionName : db.listCollectionNames()) {
            if (!whitelist.contains(collectionName)) continue;

            Document doc = db.getCollection(collectionName)
                    .find(Filters.exists(fieldName))
                    .limit(1)
                    .first();

            if (doc != null && doc.get(fieldName) != null) {
                results.add(new DiscoveryResult("MongoDB", collectionName, doc.get(fieldName)));
            }
        }
        return results;
    }
}
