package com.lumitest.service;

import com.lumitest.model.TestStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.lumitest.constant.TestConstants;
import com.lumitest.util.HeuristicUtils;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for converting natural language test scenarios into
 * structured TestSteps.
 * Uses regex-based parsing and heuristic keyword matching to interpret user
 * intent.
 */
@Service
@RequiredArgsConstructor
public class ScenarioConverterService {

    private final ElementLibraryService elementLibraryService;

    /**
     * Converts a natural language scenario string into a list of structured
     * TestSteps.
     * Automatically injects an initial 'OPEN_URL' step based on the provided
     * baseUrl.
     *
     * @param scenario The plain English scenario (e.g., "Click Login, type
     *                 password").
     * @param baseUrl  The starting URL for the test case.
     * @return A list of structured TestStep objects.
     */
    public List<TestStep> convert(String scenario, String baseUrl) {
        List<TestStep> steps = new ArrayList<>();
        int order = 1;

        // Bắt đầu bằng mở URL
        TestStep openStep = new TestStep();
        openStep.setOrder(order++);
        openStep.setDescription("Navigate to " + (baseUrl != null ? baseUrl : "application"));
        openStep.setTestData(baseUrl != null ? baseUrl : "https://www.google.com");
        openStep.setVerificationType("UI");
        steps.add(openStep);

        // Tách theo dòng, sau đó tách theo dấu phẩy hoặc chấm phẩy
        String[] lines = scenario.split("\n");
        for (String line : lines) {
            String[] segments = line.split("[,;]");
            for (String segment : segments) {
                segment = segment.trim();
                if (segment.isEmpty())
                    continue;

                processSegment(segment, baseUrl, steps, order);
                order = steps.size() + 1;
            }
        }
        return steps;
    }

    /**
     * Parses a single segment of a scenario and adds the corresponding TestStep to
     * the list.
     */
    private void processSegment(String segment, String baseUrl, List<TestStep> steps, int order) {
        String lowerSegment = segment.toLowerCase();

        // 1. INPUT actions
        if (lowerSegment.contains("input") || lowerSegment.contains("enter") || lowerSegment.contains("type")) {
            handleInputStep(segment, baseUrl, steps, order);
        }
        // 2. CLICK actions
        else if (lowerSegment.contains("click") || lowerSegment.contains("press")) {
            handleClickStep(segment, baseUrl, steps, order);
        }
        // 3. VERIFY/ASSERT actions
        else if (lowerSegment.contains("verify") || lowerSegment.contains("should see")
                || lowerSegment.contains("assert") || lowerSegment.contains("visible")) {
            handleVerifyStep(segment, steps, order);
        }
        // 4. WAIT actions
        else if (lowerSegment.contains("wait")) {
            handleWaitStep(segment, steps, order);
        }
        // 5. API actions (GET, POST, PUT, DELETE)
        else if (lowerSegment.contains("get") || lowerSegment.contains("post")) {
            handleApiStep(segment, lowerSegment, baseUrl, steps, order);
        }
    }

    private void handleInputStep(String segment, String baseUrl, List<TestStep> steps, int order) {
        String actionWord = findActionWord(segment.toLowerCase(), new String[] { "input", "enter", "type" });
        String value = extractValue(segment, actionWord + "|as|to|into");
        String label = extractLabel(segment, actionWord, value);

        String savedSelector = elementLibraryService.getSelector(baseUrl, label);
        String selector = (savedSelector != null) ? savedSelector : label;

        steps.add(createStep(order, TestConstants.Action.INPUT_TEXT, selector, value));
    }

    private void handleClickStep(String segment, String baseUrl, List<TestStep> steps, int order) {
        String actionWord = findActionWord(segment.toLowerCase(), new String[] { "click", "press" });
        String target = extractActionTarget(segment, actionWord + "|on|button|the|link");

        String savedSelector = elementLibraryService.getSelector(baseUrl, target);
        String selector = (savedSelector != null) ? savedSelector : target;

        steps.add(createStep(order, TestConstants.Action.CLICK, selector, null));
    }

    private void handleVerifyStep(String segment, List<TestStep> steps, int order) {
        String actionWord = findActionWord(segment.toLowerCase(),
                new String[] { "verify", "should see", "assert", "visible" });
        String text = extractValue(segment, actionWord + "|is");
        if (text == null)
            text = extractActionTarget(segment, actionWord + "|is|visible");

        steps.add(createStep(order, TestConstants.Action.ASSERT_TEXT, "body", text));
    }

    private void handleWaitStep(String segment, List<TestStep> steps, int order) {
        String waitVal = extractValue(segment, "wait");
        steps.add(createStep(order, TestConstants.Action.WAIT, null,
                waitVal != null ? waitVal : "3000"));
    }

    private void handleApiStep(String segment, String lowerSegment, String baseUrl, List<TestStep> steps, int order) {
        if (lowerSegment.contains("get")) {
            String url = extractValue(segment, "get");
            steps.add(createStep(order, TestConstants.Action.API_GET,
                    url != null ? url : baseUrl, null));
        } else if (lowerSegment.contains("post")) {
            String url = extractValue(segment, "post");
            steps.add(createStep(order, TestConstants.Action.API_POST,
                    url != null ? url : baseUrl, "{}"));
        }
    }

    /**
     * Finds the first matching keyword in the text from a given array of
     * candidates.
     */
    private String findActionWord(String text, String[] keywords) {
        for (String k : keywords) {
            if (text.contains(k))
                return k;
        }
        return keywords[0];
    }

    /**
     * Extracts a descriptive label (e.g., "Email", "Password") from an input
     * command.
     */
    private String extractLabel(String segment, String action, String value) {
        String lower = segment.toLowerCase();
        int start = lower.indexOf(action) + action.length();
        int end = (value != null && segment.contains(value)) ? segment.indexOf(value) : segment.length();

        if (start >= end)
            return "Input";

        String label = segment.substring(start, end).trim();
        // Loại bỏ các từ nối thừa
        label = label.replaceAll("(?i)^(with|is|as|to|into|the)\\s+", "");
        return label.isEmpty() ? "Input" : label;
    }

    /**
     * Scans for common selector patterns like CSS or quoted strings.
     */
    private String detectSelector(String line, String defaultSemantic) {
        Pattern p = Pattern.compile("\"([^\"]+)\"|(#\\S+)|(\\.\\S+)|(\\[[^\\]]+\\])");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(0).replace("\"", "");
        }
        return defaultSemantic;
    }

    /**
     * Cleans an action segment to find the target element name/label.
     */
    private String extractActionTarget(String line, String actions) {
        String target = detectSelector(line, null);
        if (target != null)
            return target;

        String cleaned = line;
        for (String action : actions.split("\\|")) {
            cleaned = cleaned.replaceAll("(?i)" + action, "");
        }
        return cleaned.replaceAll("(?i)button|the|link|on", "").trim();
    }

    /**
     * Helper to create a structured TestStep.
     */
    private TestStep createStep(int order, String action, String selector, String value) {
        TestStep step = new TestStep();
        step.setOrder(order);
        step.setDescription(action + " on " + (selector != null ? selector : "element"));
        step.setTestData(value);
        step.setVerificationType("UI");
        step.setExpectedResult("Check " + action);
        return step;
    }

    /**
     * Extracts the primary value/data (e.g., text to input, URL) from a scenario
     * segment.
     */
    private String extractValue(String line, String keywordRegex) {
        // Lấy từ cuối cùng nếu nó trông giống giá trị (không chứa space hoặc trong
        // ngoặc
        // kép)
        String[] words = line.split("\\s+");
        if (words.length > 1) {
            String lastWord = words[words.length - 1].replace("\"", "");
            // Nếu từ cuối là Alphanumeric + ký tự đặc biệt mail/phone -> coi là value
            if (lastWord.matches("[a-zA-Z0-9@._+-]+")) {
                return lastWord;
            }
        }

        Pattern pattern = Pattern.compile(
                "(?:" + keywordRegex + ") (?:with |is |as |to |into |)?\"?([a-zA-Z0-9@._+-]+|\"[^\"]+\")\"?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).replace("\"", "");
        }
        return null;
    }
}
