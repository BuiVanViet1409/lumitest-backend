package com.lumitest.service;

import com.lumitest.model.TestStep;
import com.lumitest.repository.TestStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StepService {
    private final TestStepRepository stepRepo;

    public List<TestStep> getStepsByTestCase(String testCaseId) {
        return stepRepo.findByTestCaseIdOrderByOrderAsc(testCaseId);
    }

    public TestStep save(TestStep step) {
        return stepRepo.save(step);
    }

    public void saveAll(List<TestStep> steps) {
        stepRepo.saveAll(steps);
    }

    public void delete(String id) {
        stepRepo.deleteById(id);
    }

    public void deleteAllByTestCaseId(String testCaseId) {
        stepRepo.deleteByTestCaseId(testCaseId);
    }
}
