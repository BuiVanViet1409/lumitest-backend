package com.lumitest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumitest.dto.QAResponse;
import com.lumitest.dto.QATestCase;
import com.lumitest.dto.QATestStep;
import com.lumitest.model.QAAnalysisSession;
import com.lumitest.model.TestCase;
import com.lumitest.service.QAAssistantService;
import com.lumitest.service.TestCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for the AI QA Assistant feature.
 */
@Slf4j
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QAAssistantController {

    private final QAAssistantService qaAssistantService;
    private final TestCaseService testCaseService;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint to analyze a ticket and generate test cases.
     *
     * @param request Map containing 'description' of the ticket.
     * @return Structured QAResponse.
     */
    @PostMapping("/generate")
    public QAResponse generate(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        return qaAssistantService.generateTestCases(description);
    }

    /**
     * Endpoint to save a specific generated test case.
     */
    @PostMapping("/save")
    public TestCase saveTestCase(@RequestBody Map<String, Object> request) {
        log.info("Saving individual AI-generated test case");
        @SuppressWarnings("unchecked")
        Map<String, Object> tcMap = (Map<String, Object>) request.get("testCase");
        String ticketTitle = (String) request.get("ticketTitle");

        QATestCase generated = objectMapper.convertValue(tcMap, QATestCase.class);
        TestCase testCase = qaAssistantService.saveGeneratedTestCase(generated, ticketTitle);
        TestCase savedTc = testCaseService.save(testCase);

        // Save steps
        if (generated.getSteps() != null) {
            for (int i = 0; i < generated.getSteps().size(); i++) {
                QATestStep gs = generated.getSteps().get(i);
                com.lumitest.model.TestStep step = new com.lumitest.model.TestStep();
                step.setTestCaseId(savedTc.getId());
                step.setOrder(i + 1);
                step.setDescription(gs.getDescription());
                step.setTestData(gs.getTestData());
                step.setExpectedResult(gs.getExpectedResult());
                step.setVerificationType(gs.getVerificationType());
                step.setVerificationRule(gs.getVerificationRule());
                testCaseService.saveStep(step);
            }
        }
        return savedTc;
    }

    @PostMapping("/save-all")
    public List<TestCase> saveAllTestCases(@RequestBody Map<String, Object> request) {
        log.info("Saving all AI-generated test cases");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tcMaps = (List<Map<String, Object>>) request.get("testCases");
        String ticketTitle = (String) request.get("ticketTitle");

        List<TestCase> savedList = new ArrayList<>();
        for (Map<String, Object> tcMap : tcMaps) {
            QATestCase generated = objectMapper.convertValue(tcMap, QATestCase.class);
            TestCase testCase = qaAssistantService.saveGeneratedTestCase(generated, ticketTitle);
            TestCase savedTc = testCaseService.save(testCase);

            // Save steps
            if (generated.getSteps() != null) {
                for (int i = 0; i < generated.getSteps().size(); i++) {
                    QATestStep gs = generated.getSteps().get(i);
                    com.lumitest.model.TestStep step = new com.lumitest.model.TestStep();
                    step.setTestCaseId(savedTc.getId());
                    step.setOrder(i + 1);
                    step.setDescription(gs.getDescription());
                    step.setTestData(gs.getTestData());
                    step.setExpectedResult(gs.getExpectedResult());
                    step.setVerificationType(gs.getVerificationType());
                    step.setVerificationRule(gs.getVerificationRule());
                    testCaseService.saveStep(step);
                }
            }
            savedList.add(savedTc);
        }
        return savedList;
    }

    /**
     * Endpoint to fetch all analysis sessions.
     */
    @GetMapping("/history")
    public List<QAAnalysisSession> getHistory() {
        return qaAssistantService.getAllSessions();
    }

    /**
     * Endpoint to delete an analysis session.
     */
    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable("id") String id) {
        log.info("Deleting AI analysis session: {}", id);
        qaAssistantService.deleteSession(id);
    }

    @DeleteMapping("/history")
    public void clearHistory() {
        log.warn("Clearing all AI analysis history");
        qaAssistantService.clearAllSessions();
    }
}
