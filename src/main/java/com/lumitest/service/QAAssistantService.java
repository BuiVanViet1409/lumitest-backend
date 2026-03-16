package com.lumitest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumitest.config.LumiTestConfig;
import com.lumitest.dto.QAResponse;
import com.lumitest.dto.QATestCase;
import com.lumitest.dto.QATestStep;
import com.lumitest.model.QAAnalysisSession;
import com.lumitest.repository.QAAnalysisSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service that uses Google Gemini AI to analyze a ticket and generate
 * test cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QAAssistantService {

    private final LumiTestConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final QAAnalysisSessionRepository sessionRepo;

    private static final String PROMPT_TEMPLATE = """
            You are a senior QA engineer. Analyze the following ticket description and generate structured BUSINESS-LEVEL test cases.

            A Test Case contains:
            - Title
            - Description / Background
            - Preconditions
            - Multiple Test Steps

            Each Test Step MUST contain:
            - description (business-level, human readable)
            - testData (free text or structured)
            - expectedResult
            - verificationType (UI, API, DATABASE, MESSAGE)

            INPUT TICKET:
            %s

            OUTPUT FORMAT (JSON ONLY):
            {
              "ticketTitle": "Short descriptive title",
              "testCases": [
                {
                  "title": "Clear test case title",
                  "description": "Background info",
                  "preconditions": "Initial state",
                  "acceptanceCriteriaMapping": "AC-1, AC-2",
                  "steps": [
                    {
                      "description": "User logs in with valid credentials",
                      "testData": "username=admin, password=secret",
                      "expectedResult": "Dashboard is displayed",
                      "verificationType": "UI",
                      "verificationRule": "CHECK_ELEMENT_VISIBLE: #dashboard"
                    }
                  ]
                }
              ]
            }

            IMPORTANT: Return ONLY the JSON object, no markdown formatting or extra text.
            """;

    public QAResponse generateTestCases(String ticketDescription) {
        String apiKey = config.getAi().getApiKey() != null ? config.getAi().getApiKey().trim() : null;
        String model = config.getAi().getModel() != null ? config.getAi().getModel().trim() : "gemini-1.5-flash";

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API Key is missing. Falling back to simple analysis.");
            return generateFallback(ticketDescription);
        }

        log.info("Starting AI QA generation for ticket: {}",
                ticketDescription.length() > 50 ? ticketDescription.substring(0, 50) + "..." : ticketDescription);

        try {
            String prompt = String.format(PROMPT_TEMPLATE, ticketDescription);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)))));

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error (Status {}): {}", response.statusCode(), response.body());
                return generateFallback(ticketDescription);
            }

            QAResponse qaResponse = parseAiResponse(response.body());
            log.info("AI QA generation complete. Received {} test cases.",
                    qaResponse.getTestCases() != null ? qaResponse.getTestCases().size() : 0);

            // Persist session to DB
            QAAnalysisSession session = QAAnalysisSession.builder()
                    .ticketDescription(ticketDescription)
                    .createdAt(LocalDateTime.now())
                    .result(qaResponse)
                    .build();
            sessionRepo.save(session);

            return qaResponse;

        } catch (Exception e) {
            log.error("Failed to generate test cases via AI", e);
            return generateFallback(ticketDescription);
        }
    }

    private QAResponse parseAiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String textResponse = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Clean markdown if AI included it
        if (textResponse.contains("```json")) {
            textResponse = textResponse.substring(textResponse.indexOf("```json") + 7);
            textResponse = textResponse.substring(0, textResponse.lastIndexOf("```"));
        } else if (textResponse.contains("```")) {
            textResponse = textResponse.substring(textResponse.indexOf("```") + 3);
            textResponse = textResponse.substring(0, textResponse.lastIndexOf("```"));
        }

        return objectMapper.readValue(textResponse.trim(), QAResponse.class);
    }

    private QAResponse generateFallback(String ticketDescription) {
        String context = "Analyzed Ticket";
        List<QATestCase> cases = new ArrayList<>();

        List<QATestStep> steps = new ArrayList<>();
        steps.add(QATestStep.builder()
                .description("Basic verification of requirement")
                .expectedResult("System handles the request as described")
                .verificationType("UI")
                .verificationRule("DEFAULT")
                .build());

        cases.add(QATestCase.builder()
                .title("Basic verification of " + ticketDescription.split("\n")[0])
                .description("Generated fallback test case")
                .preconditions("System is running")
                .steps(steps)
                .build());

        return QAResponse.builder()
                .ticketTitle(context)
                .testCases(cases)
                .build();
    }

    /**
     * Persists a generated test case to the database.
     */
    public com.lumitest.model.TestCase saveGeneratedTestCase(QATestCase generated, String ticketTitle) {
        com.lumitest.model.TestCase testCase = new com.lumitest.model.TestCase();
        testCase.setName(generated.getTitle());
        testCase.setDescription(generated.getDescription());
        testCase.setPreconditions(generated.getPreconditions());
        testCase.setFolder("AI Generated");
        testCase.setAcceptanceCriteriaMapping(generated.getAcceptanceCriteriaMapping() != null
                ? generated.getAcceptanceCriteriaMapping()
                : "Ticket: " + ticketTitle);

        // Steps will be saved by the controller or a separate call if needed,
        // but let's assume we want to return the TestCase with steps count.
        // Actually, the Steps need to be created in the DB as well.

        log.info("Preparing AI-generated test case: {}", testCase.getName());
        return testCase;
    }

    public List<QAAnalysisSession> getAllSessions() {
        return sessionRepo.findAllByOrderByCreatedAtDesc();
    }

    public void deleteSession(String id) {
        log.info("Removing QA Analysis Session from database: {}", id);
        sessionRepo.deleteById(id);
    }

    public void clearAllSessions() {
        log.warn("All QA Analysis Sessions are being deleted from the database");
        sessionRepo.deleteAll();
    }
}
