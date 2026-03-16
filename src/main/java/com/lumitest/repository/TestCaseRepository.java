package com.lumitest.repository;

import com.lumitest.model.TestCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends MongoRepository<TestCase, String> {
    org.springframework.data.domain.Page<TestCase> findByFolder(String folder,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.mongodb.repository.Aggregation(pipeline = { "{ '$group': { '_id': '$folder' } }" })
    java.util.List<String> findDistinctFolders();
}
