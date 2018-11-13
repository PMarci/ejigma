package camel.enigma.util;

import camel.enigma.io.KeyBoardEndpoint;
import camel.enigma.model.Rotor;
import camel.enigma.model.Scrambler;

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

    private int result;
    private char resultAsChar;
    private List<HistoryEntry> history;

    private char offsetAsChar;

    public ScrambleResult(Character resultAsChar) {
        history = new ArrayList<>();
        putResult(0, resultAsChar, resultAsChar, resultAsChar, INPUT_STRING, 0, 'A');
        putDefaultCharInputToIntResult();
    }

    public void putDefaultCharInputToIntResult() {
        putDefaultCharInputToIntResult(this);
    }

    public void putCharInputToIntResult(String alphabetString) {
        putCharInputToIntResult(alphabetString, this);
    }

    public static ScrambleResult putDefaultCharInputToIntResult(ScrambleResult input) {
        return putCharInputToIntResult(Scrambler.DEFAULT_ALPHABET_STRING, input);
    }

    public static ScrambleResult putCharInputToIntResult(String alphabetString, ScrambleResult input) {
        int wheelPos = alphabetString.indexOf(input.getResultAsChar());
        input.setResult(wheelPos);
        return input;
    }

    public char getResultAsChar() {
        return resultAsChar;
    }

    public ScrambleResult putResult(int result, char wiringInput, char wiringOutput, char resultAsChar, String stepId, int offset, char offsetAsChar) {
        this.result = result;
        this.resultAsChar = resultAsChar;
        this.offsetAsChar = offsetAsChar;
        addHistoryEntry(wiringInput, wiringOutput, stepId, offset, offsetAsChar);
        return this;
    }

    private void addHistoryEntry(char wiringInput, char wiringOutput, String stepId, int offset, char offsetAsChar) {
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
            history.add(new HistoryEntry(wiringInput, wiringOutput, stepId, offset, offsetAsChar));
        } else if (passNo == 0) {
            history.add(new HistoryEntry(wiringInput, wiringOutput, stepId + STEP_SEPARATOR + 2, offset, offsetAsChar));
        } else {
            history.add(new HistoryEntry(wiringInput, wiringOutput, stationId + STEP_SEPARATOR + ++passNo, offset, offsetAsChar));
        }
    }

    public void recordOutput() {
        HistoryEntry last = history.get(history.size() - 1);
        Character lastValue = last.getWiringOutput();
        // assuming no rotation since last...
        // TODO figure out association with keyboard/output
        int lastOffset = last.getOffset();
        history.add(new HistoryEntry(
            lastValue, Rotor.subtractOffset(KeyBoardEndpoint.DEFAULT_ALPHABET, lastValue, lastOffset), OUTPUT_STRING,
            lastOffset,
                offsetAsChar));
    }

    public void setResultAsChar(char resultAsChar) {
        this.resultAsChar = resultAsChar;
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

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    private class HistoryEntry {

        private char wiringInput;
        private char wiringOutput;

        private String stationId;
        private int offset;
        private char offsetAsChar;

        public HistoryEntry(char wiringInput, char wiringOutput, String stationId, int offset, char offsetAsChar) {
            this.wiringInput = wiringInput;
            this.wiringOutput = wiringOutput;
            this.stationId = stationId;
            this.offset = offset;
            this.offsetAsChar = offsetAsChar;
        }

        @Override
        public String toString() {
            return stationId + " : " + wiringInput + ":::>" + wiringOutput + ", offset = " + offsetAsChar;
        }

        public Character getWiringInput() {
            return wiringInput;
        }

        public void setWiringInput(Character wiringInput) {
            this.wiringInput = wiringInput;
        }

        public Character getWiringOutput() {
            return wiringOutput;
        }

        public void setWiringOutput(Character wiringOutput) {
            this.wiringOutput = wiringOutput;
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
