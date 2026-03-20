package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoReleaseRepository extends MongoRepository<ReleaseEntity, String> {
}
