package com.lumitest.service;

import com.lumitest.model.TestCase;
import com.lumitest.repository.TestCaseRepository;
import com.lumitest.repository.TestStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TestCaseService {
    @Autowired
    private TestCaseRepository testCaseRepo;

    @Autowired
    private TestStepRepository stepRepo;

    public List<TestCase> getAll() {
        List<TestCase> list = testCaseRepo.findAll();
        list.forEach(tc -> tc.setStepsCount(stepRepo.countByTestCaseId(tc.getId())));
        return list;
    }

    public TestCase getById(String id) {
        TestCase tc = testCaseRepo.findById(id).orElseThrow();
        tc.setStepsCount(stepRepo.countByTestCaseId(id));
        return tc;
    }

    public TestCase save(TestCase tc) {
        return testCaseRepo.save(tc);
    }

    public void delete(String id) {
        stepRepo.deleteByTestCaseId(id);
        testCaseRepo.deleteById(id);
    }
}
