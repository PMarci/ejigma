package camel.enigma.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrambleResult {

    private static final String STEP_SEPARATOR = "/";
    private static final String HURR = "I/2";
    private static final String STEP_REGEX = "^([^/]+)(?:" + STEP_SEPARATOR + "(\\d+))?$";
    private static final Pattern STEP_PATTERN = Pattern.compile(STEP_REGEX);

    public ScrambleResult(char result) {
        this.result = result;
        history = new LinkedHashMap<>();
    }

    private char result;
    private Map<String, Character> history;

    public char getResult() {
        return result;
    }

    public void putResult(char result, String stepId) {
        this.result = result;
        // TODO improve/normalize
        Matcher stepMatcher = STEP_PATTERN.matcher(stepId);
        String stationId = null;
        String passNoString = null;
        if (stepMatcher.matches()) {
            stationId = stepMatcher.group(1);
            passNoString = stepMatcher.group(2);
        }
        int passNo = (passNoString != null) ? Integer.valueOf(passNoString) : 0;
        if (!history.containsKey(stepId)) {
            history.put(stepId, result);
        } else if (passNo == 0) {
            history.put(stepId + STEP_SEPARATOR + 2, result);
        } else {
            history.put(stationId + STEP_SEPARATOR + ++passNo, result);
        }
    }

    // TODO wat do
    public void setResult(char result) {
        this.result = result;
    }

    public Map<String, Character> getHistory() {
        return history;
    }

    public void setHistory(Map<String, Character> history) {
        this.history = history;
    }
}
