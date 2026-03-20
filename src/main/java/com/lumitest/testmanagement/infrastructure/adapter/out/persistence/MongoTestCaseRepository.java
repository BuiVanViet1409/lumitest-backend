package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import java.util.List;

public interface MongoTestCaseRepository extends MongoRepository<TestCaseEntity, String> {
    List<TestCaseEntity> findByFolder(String folder);
    List<TestCaseEntity> findByReleaseId(String releaseId);
    List<TestCaseEntity> findByExternalRequirementId(String externalRequirementId);

    @Aggregation(pipeline = { "{ '$group': { '_id': '$folder' } }" })
    List<String> findDistinctFolders();
}
