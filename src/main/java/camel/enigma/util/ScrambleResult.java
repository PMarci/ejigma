package camel.enigma.util;

import camel.enigma.io.KeyBoardEndpoint;
import camel.enigma.model.Rotor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrambleResult {

    private static final String INPUT_STRING = "INPUT";
    private static final String OUTPUT_STRING = "OUTPUT";

    private static final String STEP_SEPARATOR = "/";
    private static final String STEP_REGEX = "^([^/]+)(?:" + STEP_SEPARATOR + "(\\d+))?$";
    private static final Pattern STEP_PATTERN = Pattern.compile(STEP_REGEX);

    private char result;
    private List<HistoryEntry> history;

    // TODO find a way to not rely on external state of result
    private char offsetAsChar;

    public ScrambleResult(char result) {
        history = new ArrayList<>();
        putResult(result, INPUT_STRING, result, result, 0, 'A');
    }

    public char getResult() {
        return result;
    }

    public void putResult(char result, String stepId, char key, char value, int offset, char offsetAsChar) {
        this.result = result;
        this.offsetAsChar = offsetAsChar;
        // TODO improve/normalize
        addHistoryEntry(stepId, key, value, offset, offsetAsChar);
    }

    private void addHistoryEntry(String stepId, char key, char value, int offset, char offsetAsChar) {
        Matcher stepMatcher = STEP_PATTERN.matcher(stepId);
        String stationId = null;
        String passNoString = null;
        if (stepMatcher.matches()) {
            stationId = stepMatcher.group(1);
            passNoString = stepMatcher.group(2);
        }
        int passNo = (passNoString != null) ? Integer.valueOf(passNoString) : 0;
        boolean stepVisited = history.stream().sequential().anyMatch(historyEntry -> historyEntry.getStationId()
                .equals(stepId));
        if (!stepVisited) {
            history.add(new HistoryEntry(stepId, key, value, offset, offsetAsChar));
        } else if (passNo == 0) {
            history.add(new HistoryEntry(stepId + STEP_SEPARATOR + 2, key, value, offset, offsetAsChar));
        } else {
            history.add(new HistoryEntry(stationId + STEP_SEPARATOR + ++passNo, key, value, offset, offsetAsChar));
        }
    }

    public void recordOutput() {
        HistoryEntry last = history.get(history.size() - 1);
        Character lastValue = last.getValue();
        // assuming no rotation since last...
        // TODO figure out association with keyboard/output
        int lastOffset = last.getOffset();
        history.add(new HistoryEntry(
                OUTPUT_STRING,
                lastValue,
                Rotor.subtractOffset(KeyBoardEndpoint.DEFAULT_ALPHABET, lastValue, lastOffset),
                lastOffset,
                offsetAsChar));
    }

    // TODO wat do
    public void setResult(char result) {
        this.result = result;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryEntry> history) {
        this.history = history;
    }

    public char getOffsetAsChar() {
        return offsetAsChar;
    }

    private class HistoryEntry {

        private char key;
        private char value;

        private String stationId;
        private int offset;
        private char offsetAsChar;

        public HistoryEntry(String stationId, char key, char value, int offset, char offsetAsChar) {
            this.key = key;
            this.value = value;
            this.stationId = stationId;
            this.offset = offset;
            this.offsetAsChar = offsetAsChar;
        }

        @Override
        public String toString() {
            return stationId + " : " + key + ":::>" + value + ", offset = " + offsetAsChar;
        }

        public Character getKey() {
            return key;
        }

        public void setKey(Character key) {
            this.key = key;
        }

        public Character getValue() {
            return value;
        }

        public void setValue(Character value) {
            this.value = value;
        }

        public Character getOffsetAsChar() {
            return offsetAsChar;
        }

        public void setOffsetAsChar(Character offsetAsChar) {
            this.offsetAsChar = offsetAsChar;
        }

        public String getStationId() {
            return stationId;
        }

        public void setStationId(String stationId) {
            this.stationId = stationId;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }
    }
}
