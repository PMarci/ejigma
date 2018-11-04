package camel.enigma.util;

import java.util.Map;

public class ScrambleResult {

    private char result;
    private Map<String, Character> history;

    public char getResult() {
        return result;
    }

    public void putResult(char result, String stepId) {
        this.result = result;
        history.put(stepId, result);
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
