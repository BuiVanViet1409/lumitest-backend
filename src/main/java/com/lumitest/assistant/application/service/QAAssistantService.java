package com.lumitest.assistant.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumitest.assistant.domain.model.QAAnalysisSession;
import com.lumitest.assistant.domain.port.in.GenerateTestCasesUseCase;
import com.lumitest.assistant.domain.port.out.QAAnalysisRepositoryPort;
import com.lumitest.assistant.infrastructure.adapter.in.web.QAResponse;
import com.lumitest.assistant.infrastructure.adapter.in.web.QATestCase;
import com.lumitest.assistant.infrastructure.adapter.in.web.QATestStep;
import com.lumitest.config.LumiTestConfig;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class QAAssistantService implements GenerateTestCasesUseCase {

    private final LumiTestConfig config;
    private final ObjectMapper objectMapper;
    private final QAAnalysisRepositoryPort repositoryPort;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String PROMPT_TEMPLATE = """
            You are a senior QA engineer. Analyze the following ticket description and generate structured BUSINESS-LEVEL test cases.
            INPUT TICKET:
            %s
            Return ONLY the JSON object.
            """;

    @Override
    public QAResponse generateTestCases(String ticketDescription) {
        String apiKey = config.getAi().getApiKey();
        if (apiKey == null || apiKey.isEmpty()) return generateFallback(ticketDescription);

        try {
            String prompt = String.format(PROMPT_TEMPLATE, ticketDescription);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + config.getAi().getModel() + ":generateContent?key=" + apiKey;

            Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                QAResponse qaResponse = parseAiResponse(response.body());
                QAAnalysisSession session = QAAnalysisSession.builder()
                        .question(ticketDescription)
                        .answer(response.body())
                        .createdAt(LocalDateTime.now())
                        .build();
                repositoryPort.save(session);
                return qaResponse;
            }
        } catch (Exception e) {
            log.error("AI Generation failed", e);
        }
        return generateFallback(ticketDescription);
    }

    private QAResponse parseAiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String textResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        if (textResponse.contains("```json")) {
            textResponse = textResponse.substring(textResponse.indexOf("```json") + 7);
            textResponse = textResponse.substring(0, textResponse.lastIndexOf("```"));
        }
        return objectMapper.readValue(textResponse.trim(), QAResponse.class);
    }

    private QAResponse generateFallback(String ticketDescription) {
        List<QATestCase> cases = new ArrayList<>();
        cases.add(QATestCase.builder()
                .title("Basic verification")
                .description("Fallback test case")
                .steps(List.of(QATestStep.builder()
                        .description("Verify ticket requirement")
                        .expectedResult("Success")
                        .verificationType("UI")
                        .build()))
                .build());
        return QAResponse.builder()
                .ticketTitle("Fallback")
                .testCases(cases)
                .build();
    }

    @Override
    public List<QAAnalysisSession> getAllSessions() {
        return repositoryPort.findAll();
    }

    @Override
    public List<String> suggestFields(String intent) {
        String apiKey = config.getAi().getApiKey();
        if (apiKey == null || apiKey.isEmpty()) return List.of("user_id", "status", "created_at");

        try {
            String prompt = String.format("As a database expert, suggest 5-10 technical field names (snake_case) relevant to this intent: '%s'. Return ONLY a JSON list of strings.", intent);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + config.getAi().getModel() + ":generateContent?key=" + apiKey;

            Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                if (text.contains("```json")) {
                    text = text.substring(text.indexOf("```json") + 7);
                    text = text.substring(0, text.lastIndexOf("```"));
                }
                return objectMapper.readValue(text.trim(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.error("Field suggestion failed", e);
        }
        return List.of("error_recovering_suggestions");
    }

    @Override
    public void deleteSession(String id) {
        repositoryPort.deleteById(id);
    }

    @Override
    public void clearAllSessions() {
        repositoryPort.findAll().forEach(s -> repositoryPort.deleteById(s.getId()));
    }
}
