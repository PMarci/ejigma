package camel.enigma.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrambleResult {

    private static final String INPUT_STRING = "INPUT";;
    private static final String OUTPUT_STRING = "OUTPUT";;

    private static final String STEP_SEPARATOR = "/";
    private static final String STEP_REGEX = "^([^/]+)(?:" + STEP_SEPARATOR + "(\\d+))?$";
    private static final Pattern STEP_PATTERN = Pattern.compile(STEP_REGEX);

    private char result;
    private List<HistoryEntry> history;

    // TODO find a way to not rely on external state of result
    private char offset;

    public ScrambleResult(char result) {
        history = new ArrayList<>();
        putResult(result, INPUT_STRING, result, result, 'A');
    }

    public char getResult() {
        return result;
    }

    public void putResult(char result, String stepId, char key, char value, char offset) {
        this.result = result;
        this.offset = offset;
        // TODO improve/normalize
        addHistoryEntry(stepId, key, value, offset);
    }

    private void addHistoryEntry(String stepId, char key, char value, char offset) {
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
            history.add(new HistoryEntry(stepId, key, value, offset));
        } else if (passNo == 0) {
            history.add(new HistoryEntry(stepId + STEP_SEPARATOR + 2, key, value, offset));
        } else {
            history.add(new HistoryEntry(stationId + STEP_SEPARATOR + ++passNo, key, value, offset));
        }
    }

    public void recordOutput() {
        HistoryEntry last = history.get(history.size() - 1);
        Character lastValue = last.getValue();
        history.add(new HistoryEntry(OUTPUT_STRING, lastValue, Util.wrapOverflow(lastValue - Util.offsetToIndex(offset)), offset));
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

    public char getOffset() {
        return offset;
    }

    private class HistoryEntry {

        private Character key;
        private Character value;

        private String stationId;
        private Character offset;

        public HistoryEntry(String stationId, Character key, Character value, Character offset) {
            this.key = key;
            this.value = value;
            this.stationId = stationId;
            this.offset = offset;
        }

        public HistoryEntry(Character key, Character value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return stationId + " : " + key + ":::>" + value + ", offset = " + offset;
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

        public Character getOffset() {
            return offset;
        }

        public void setOffset(Character offset) {
            this.offset = offset;
        }

        public String getStationId() {
            return stationId;
        }

        public void setStationId(String stationId) {
            this.stationId = stationId;
        }
    }
}
