package com.lumitest.repository;

import com.lumitest.model.Release;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseRepository extends MongoRepository<Release, String> {
}
