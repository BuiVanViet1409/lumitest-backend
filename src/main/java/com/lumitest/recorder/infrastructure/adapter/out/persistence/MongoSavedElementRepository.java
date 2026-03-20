package com.lumitest.recorder.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSavedElementRepository extends MongoRepository<SavedElementEntity, String> {
}
