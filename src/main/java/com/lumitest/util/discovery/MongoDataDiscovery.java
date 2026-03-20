package com.lumitest.util.discovery;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * MongoDB implementation of DataDiscoveryTool.
 * Searches all collections for the first document containing the specified field.
 */
public class MongoDataDiscovery implements DataDiscoveryTool {

    private final MongoTemplate mongoTemplate;

    public MongoDataDiscovery(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<DiscoveryResult> discover(String fieldName) {
        MongoDatabase database = mongoTemplate.getDb();
        
        for (String collectionName : database.listCollectionNames()) {
            Document doc = database.getCollection(collectionName)
                    .find(Filters.exists(fieldName))
                    .first();
            
            if (doc != null && doc.get(fieldName) != null) {
                return Optional.of(new DiscoveryResult(collectionName, doc.get(fieldName)));
            }
        }
        
        return Optional.empty();
    }

    @Override
    public java.util.List<String> listCollections() {
        java.util.List<String> collections = new java.util.ArrayList<>();
        mongoTemplate.getDb().listCollectionNames().into(collections);
        return collections;
    }
}
