package com.lumitest.testmanagement.application.service;

import com.lumitest.testmanagement.domain.model.TestStep;
import com.lumitest.recorder.application.service.ElementLibraryService;
import com.lumitest.constant.TestConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ScenarioConverterService {

    private final ElementLibraryService elementLibraryService;

    public List<TestStep> convert(String scenario, String baseUrl) {
        List<TestStep> steps = new ArrayList<>();
        int order = 1;

        TestStep openStep = TestStep.builder()
                .order(order++)
                .description("Navigate to " + (baseUrl != null ? baseUrl : "application"))
                .testData(baseUrl != null ? baseUrl : "https://www.google.com")
                .verificationType("UI")
                .build();
        steps.add(openStep);

        String[] lines = scenario.split("\n");
        for (String line : lines) {
            String[] segments = line.split("[,;]");
            for (String segment : segments) {
                segment = segment.trim();
                if (segment.isEmpty()) continue;
                processSegment(segment, baseUrl, steps, order);
                order = steps.size() + 1;
            }
        }
        return steps;
    }

    private void processSegment(String segment, String baseUrl, List<TestStep> steps, int order) {
        String lowerSegment = segment.toLowerCase();
        if (lowerSegment.contains("input") || lowerSegment.contains("enter") || lowerSegment.contains("type")) {
            handleInputStep(segment, baseUrl, steps, order);
        } else if (lowerSegment.contains("click") || lowerSegment.contains("press")) {
            handleClickStep(segment, baseUrl, steps, order);
        } else if (lowerSegment.contains("verify") || lowerSegment.contains("should see") || lowerSegment.contains("assert") || lowerSegment.contains("visible")) {
            handleVerifyStep(segment, steps, order);
        } else if (lowerSegment.contains("wait")) {
            handleWaitStep(segment, steps, order);
        }
    }

    private void handleInputStep(String segment, String baseUrl, List<TestStep> steps, int order) {
        String actionWord = findActionWord(segment.toLowerCase(), new String[] { "input", "enter", "type" });
        String value = extractValue(segment, actionWord + "|as|to|into");
        String label = extractLabel(segment, actionWord, value);
        String selector = elementLibraryService.getSelector(baseUrl, label);
        if (selector == null) selector = label;
        steps.add(createStep(order, "INPUT_TEXT", selector, value));
    }

    private void handleClickStep(String segment, String baseUrl, List<TestStep> steps, int order) {
        String actionWord = findActionWord(segment.toLowerCase(), new String[] { "click", "press" });
        String target = extractActionTarget(segment, actionWord + "|on|button|the|link");
        String selector = elementLibraryService.getSelector(baseUrl, target);
        if (selector == null) selector = target;
        steps.add(createStep(order, "CLICK", selector, null));
    }

    private void handleVerifyStep(String segment, List<TestStep> steps, int order) {
        String actionWord = findActionWord(segment.toLowerCase(), new String[] { "verify", "should see", "assert", "visible" });
        String text = extractValue(segment, actionWord + "|is");
        if (text == null) text = extractActionTarget(segment, actionWord + "|is|visible");
        steps.add(createStep(order, "ASSERT_TEXT", "body", text));
    }

    private void handleWaitStep(String segment, List<TestStep> steps, int order) {
        String waitVal = extractValue(segment, "wait");
        steps.add(createStep(order, "WAIT", null, waitVal != null ? waitVal : "3000"));
    }

    private String findActionWord(String text, String[] keywords) {
        for (String k : keywords) { if (text.contains(k)) return k; }
        return keywords[0];
    }

    private String extractLabel(String segment, String action, String value) {
        String lower = segment.toLowerCase();
        int start = lower.indexOf(action) + action.length();
        int end = (value != null && segment.contains(value)) ? segment.indexOf(value) : segment.length();
        if (start >= end) return "Input";
        String label = segment.substring(start, end).trim();
        label = label.replaceAll("(?i)^(with|is|as|to|into|the)\\s+", "");
        return label.isEmpty() ? "Input" : label;
    }

    private String extractActionTarget(String line, String actions) {
        String target = detectSelector(line, null);
        if (target != null) return target;
        String cleaned = line;
        for (String action : actions.split("\\|")) { cleaned = cleaned.replaceAll("(?i)" + action, ""); }
        return cleaned.replaceAll("(?i)button|the|link|on", "").trim();
    }

    private String detectSelector(String line, String defaultSemantic) {
        Pattern p = Pattern.compile("\"([^\"]+)\"|(#\\S+)|(\\.\\S+)|(\\[[^\\]]+\\])");
        Matcher m = p.matcher(line);
        if (m.find()) return m.group(0).replace("\"", "");
        return defaultSemantic;
    }

    private TestStep createStep(int order, String action, String selector, String value) {
        return TestStep.builder()
                .order(order)
                .description(action + " on " + (selector != null ? selector : "element"))
                .testData(value)
                .verificationType("UI")
                .expectedResult("Check " + action)
                .build();
    }

    private String extractValue(String line, String keywordRegex) {
        String[] words = line.split("\\s+");
        if (words.length > 1) {
            String lastWord = words[words.length - 1].replace("\"", "");
            if (lastWord.matches("[a-zA-Z0-9@._+-]+")) return lastWord;
        }
        Pattern pattern = Pattern.compile("(?:" + keywordRegex + ") (?:with |is |as |to |into |)?\"?([a-zA-Z0-9@._+-]+|\"[^\"]+\")\"?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) return matcher.group(1).replace("\"", "");
        return null;
    }
}
