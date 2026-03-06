package com.lumitest.service;

import com.lumitest.model.TestStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.lumitest.util.HeuristicUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ScenarioConverterService {

    @Autowired
    private ElementLibraryService elementLibraryService;

    /**
     * Chuyển đổi kịch bản ngôn ngữ tự nhiên thành danh sách các bước có cấu trúc.
     * Đây là phiên bản Heuristic/Regex-based mô phỏng AI.
     */
    public List<TestStep> convert(String scenario, String baseUrl) {
        List<TestStep> steps = new ArrayList<>();
        int order = 1;

        // Bắt đầu bằng mở URL
        TestStep openStep = new TestStep();
        openStep.setOrder(order++);
        openStep.setAction(com.lumitest.constant.TestConstants.Action.OPEN_URL);
        openStep.setValue(baseUrl != null ? baseUrl : "https://www.google.com");
        steps.add(openStep);

        // Tách theo dòng, sau đó tách theo dấu phẩy hoặc chấm phẩy
        String[] lines = scenario.split("\n");
        for (String line : lines) {
            String[] segments = line.split("[,;]");
            for (String segment : segments) {
                segment = segment.trim();
                if (segment.isEmpty())
                    continue;

                String lowerSegment = segment.toLowerCase();

                // 1. Phân tích: INPUT (Email, Phone, Username...)
                if (lowerSegment.contains("input") || lowerSegment.contains("enter") || lowerSegment.contains("type")) {
                    String actionWord = findActionWord(lowerSegment, new String[] { "input", "enter", "type" });
                    String value = extractValue(segment, actionWord + "|as|to|into");

                    // Lấy phần ở giữa Action và Value làm Label để giống ý người dùng
                    String label = extractLabel(segment, actionWord, value);

                    // Ưu tiên tra cứu thư viện nếu có selector xịn cho lable này
                    String savedSelector = elementLibraryService.getSelector(baseUrl, label);
                    String selector = (savedSelector != null) ? savedSelector : label;

                    steps.add(createStep(order++, com.lumitest.constant.TestConstants.Action.INPUT_TEXT, selector,
                            value));
                }
                // 2. Phân tích: CLICK
                else if (lowerSegment.contains("click") || lowerSegment.contains("press")) {
                    String actionWord = findActionWord(lowerSegment, new String[] { "click", "press" });
                    String target = extractActionTarget(segment, actionWord + "|on|button|the|link");

                    String savedSelector = elementLibraryService.getSelector(baseUrl, target);
                    String selector = (savedSelector != null) ? savedSelector : target;
                    steps.add(createStep(order++, com.lumitest.constant.TestConstants.Action.CLICK, selector, null));
                }
                // 3. Phân tích: VERIFY/ASSERT
                else if (lowerSegment.contains("verify") || lowerSegment.contains("should see")
                        || lowerSegment.contains("assert") || lowerSegment.contains("visible")) {
                    String actionWord = findActionWord(lowerSegment,
                            new String[] { "verify", "should see", "assert", "visible" });
                    String text = extractValue(segment, actionWord + "|is");
                    if (text == null)
                        text = extractActionTarget(segment, actionWord + "|is|visible");
                    steps.add(
                            createStep(order++, com.lumitest.constant.TestConstants.Action.ASSERT_TEXT, "body", text));
                }
                // 4. Phân tích: WAIT
                else if (lowerSegment.contains("wait")) {
                    String waitVal = extractValue(segment, "wait");
                    steps.add(createStep(order++, com.lumitest.constant.TestConstants.Action.WAIT, null,
                            waitVal != null ? waitVal : "3000"));
                }
            }
        }
        return steps;
    }

    private String findActionWord(String text, String[] keywords) {
        for (String k : keywords) {
            if (text.contains(k))
                return k;
        }
        return keywords[0];
    }

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

    private String detectSelector(String line, String defaultSemantic) {
        Pattern p = Pattern.compile("\"([^\"]+)\"|(#\\S+)|(\\.\\S+)|(\\[[^\\]]+\\])");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(0).replace("\"", "");
        }
        return defaultSemantic;
    }

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

    private TestStep createStep(int order, String action, String selector, String value) {
        TestStep step = new TestStep();
        step.setOrder(order);
        step.setAction(action);
        step.setSelector(selector);
        step.setValue(value);
        return step;
    }

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
