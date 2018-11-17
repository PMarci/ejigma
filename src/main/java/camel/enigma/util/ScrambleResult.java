package camel.enigma.util;

import camel.enigma.io.KeyBoardEndpoint;
import camel.enigma.model.Rotor;
import camel.enigma.model.Scrambler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
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

    private Character offsetAsChar;

    public ScrambleResult(Character resultAsChar) {
        history = new ArrayList<>();
        putResult(0, resultAsChar, resultAsChar, resultAsChar, INPUT_STRING);
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

    public ScrambleResult putResult(
        int result,
        char wiringInput,
        char wiringOutput,
        char resultAsChar,
        String stepId,
        int offset,
        char offsetAsChar) {

        this.result = result;
        this.resultAsChar = resultAsChar;
        this.offsetAsChar = offsetAsChar;
        addHistoryEntry(wiringInput, wiringOutput, stepId, offset, offsetAsChar);
        return this;
    }

    public ScrambleResult putResult(
        int result,
        char wiringInput,
        char wiringOutput,
        char resultAsChar,
        String stepId) {

        this.result = result;
        this.resultAsChar = resultAsChar;
        addHistoryEntry(wiringInput, wiringOutput, stepId);
        return this;
    }

    private void addHistoryEntry(char wiringInput,
        char wiringOutput,
        String stepId,
        Integer offset,
        Character offsetAsChar) {
        HistoryEntry newEntry = generateHistoryEntry(wiringInput, wiringOutput, stepId);
        newEntry.setOffset(offset);
        newEntry.setOffsetAsChar(offsetAsChar);
        history.add(newEntry);
    }

    private void addHistoryEntry(char wiringInput, char wiringOutput, String stepId) {
        HistoryEntry newEntry = generateHistoryEntry(wiringInput, wiringOutput, stepId);
        history.add(newEntry);
    }

    private HistoryEntry generateHistoryEntry(char wiringInput, char wiringOutput, String stepId) {
        Matcher stepMatcher = STEP_PATTERN.matcher(stepId);
        String stationId = null;
        String passNoString = null;
        if (stepMatcher.matches()) {
            stationId = stepMatcher.group(1);
            passNoString = stepMatcher.group(2);
        }
        int passNo = (passNoString != null) ? Integer.valueOf(passNoString) : 0;
        boolean stepVisited = history.stream().sequential()
            .anyMatch(historyEntry -> historyEntry.getStationId().equals(stepId));
        HistoryEntry newEntry;
        if (!stepVisited) {
            newEntry = new HistoryEntry(wiringInput, wiringOutput, result, stepId);
        } else if (passNo == 0) {
            newEntry = new HistoryEntry(wiringInput, wiringOutput, result, stepId + STEP_SEPARATOR + 2);
        } else {
            newEntry = new HistoryEntry(wiringInput, wiringOutput, result, stationId + STEP_SEPARATOR + ++passNo);
        }
        return newEntry;
    }

    public void recordOutput() {
        HistoryEntry last = history.get(history.size() - 1);
        Character lastValue = last.getWiringOutput();
        // assuming no rotation since last...
        // TODO figure out association with keyboard/output
        Integer lastOffset = last.getOffset();
        HistoryEntry newEntry = new HistoryEntry(
            lastValue,
            Rotor.subtractOffset(KeyBoardEndpoint.DEFAULT_ALPHABET, lastValue, lastOffset),
            result,
            OUTPUT_STRING);
        // TODO unneeded if we can assume static entry wheel and output always after it
        if (lastOffset != null) {
            newEntry.setOffset(lastOffset);
        }
        history.add(newEntry);
    }

    public String printHistory() {
        StringBuilder sb = new StringBuilder();
        int historySize = history.size();
        int lastOutputIndex = (historySize > 0) ?
            Util.indexOf(Scrambler.DEFAULT_ALPHABET, history.get(historySize - 1).getWiringOutput()) :
            0;
        List<String> letterLines = HistoryEntry.letters[lastOutputIndex];
        Iterator<String> letterLineIterator = letterLines.iterator();
        for (int i = 0; i < historySize || i < HistoryEntry.HEIGHT; i++) {
            HistoryEntry historyEntry;
            String historyEntryBlock;
            String letterLine1 = null;
            String letterLine2 = null;
            if (i < historySize) {
                historyEntry = history.get(i);
                if (letterLineIterator.hasNext()) {
                    letterLine1 = letterLineIterator.next();
                }
                if (letterLineIterator.hasNext()) {
                    letterLine2 = letterLineIterator.next();
                }
                historyEntryBlock = historyEntry.toSandvichString(letterLine1, letterLine2);
                sb.append(historyEntryBlock);
            }
            sb.append('\n');
        }
        return sb.toString();
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

    public static class HistoryEntry {

        private static final List[] letters = initLetters();

        private static final int HEIGHT = 15;

        private static final int PADDING = 20;
        private static final int SECOND_PADDING = 60;
        private char wiringInput;

        private char wiringOutput;
        private int result;
        private String stationId;

        private Integer offset;
        private Character offsetAsChar;

        HistoryEntry(char wiringInput, char wiringOutput, int result, String stationId) {
            this.wiringInput = wiringInput;
            this.wiringOutput = wiringOutput;
            this.result = result;
            this.stationId = stationId;
        }

        @Override
        public String toString() {
            int terminal;
            if (stationId.equals(INPUT_STRING)) {
                terminal = -1;
            } else {
                if (stationId.equals(OUTPUT_STRING)) {
                    terminal = 1;
                } else {
                    terminal = 0;
                }
            }
            String wiringFirstPart = (terminal == -1) ? " " + wiringInput + " :::::> " : "╚► " + wiringInput;
            String wiringSecondPart = (terminal == 0) ? " :::> " : "";
            String wiringThirdPart = (terminal == 1) ? " :::::> " + String.valueOf(wiringOutput) : String.valueOf(wiringOutput) + " ═╗\n";
            String returnBranch = getPadding("") + "   ╔════[" + Scrambler.DEFAULT_ALPHABET[result] + "]═════╝";
            String wiringPart = wiringFirstPart + wiringSecondPart + wiringThirdPart + ((terminal != 1) ? returnBranch : "");
            String mainPart = pad(stationId) + " : " + wiringPart;
            return ((getOffsetAsChar() != null) ? mainPart + ", offset = " + offsetAsChar : mainPart);
        }

        public String toSandvichString(String letterLine1, String letterLine2) {
            StringBuilder stringBuilder = new StringBuilder();
            int terminal;
            if (stationId.equals(INPUT_STRING)) {
                terminal = -1;
            } else {
                if (stationId.equals(OUTPUT_STRING)) {
                    terminal = 1;
                } else {
                    terminal = 0;
                }
            }
            stringBuilder.append(pad(stationId)).append(" : ");
            String wiringFirstPart = (terminal == -1) ? " " + wiringInput + " :::::> " : "╚► " + wiringInput;
            String wiringSecondPart = (terminal == 0) ? " :::> " : "";
            String wiringThirdPart = (terminal == 1) ? " :::::> " + wiringOutput : String.valueOf(wiringOutput) + " ═╗";
            String returnBranch = getPadding("") + "   ╔════[" + Scrambler.DEFAULT_ALPHABET[result] + "]═════╝";
            stringBuilder.append(wiringFirstPart).append(wiringSecondPart).append(wiringThirdPart);
            int firstLineLength = stringBuilder.length();
            if (letterLine1 != null) {
                stringBuilder.append(getPadding(firstLineLength, SECOND_PADDING)).append(letterLine1);
                firstLineLength = stringBuilder.length();
            }
            if (terminal != 1) {
                stringBuilder.append('\n');
                stringBuilder.append(returnBranch);
            }
            if (getOffsetAsChar() != null) {
                stringBuilder.append(", offset = ").append(offsetAsChar);
            }
            int secondLineLength = stringBuilder.length() - firstLineLength - 1;
            if (letterLine2 != null) {
                stringBuilder.append(getPadding(secondLineLength, SECOND_PADDING)).append(letterLine2);
            }
            return stringBuilder.toString();
        }

        public static String getPadding(String s) {
            return getPadding(s, PADDING);
        }

        public static String getPadding(String s, int padding) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getSpace(s, padding); i++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        public static String getPadding(int strLen, int padding) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getSpace(strLen, padding); i++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        private static int getSpace(String s, int padding) {
            return getSpace(s.length(), padding);
        }

        private static int getSpace(int strLen, int padding) {
            return (padding > strLen) ? padding - strLen : 0;
        }

        private static String pad(String s) {
            return pad(s, PADDING);
        }

        public static String pad(String s, int padding) {
            return s.concat(getPadding(s, padding));
        }

        private static List[] initLetters() {
            List[] result = new ArrayList[28];
            for (int i = 0; i < 28; i++) {
                result[i] = loadLetter(i);
            }
            return result;
        }

        public static List<String> loadLetter(int index) {

            int delimiters = 0;
            int i = 0;
            Path file = Paths.get("src/main/resources/", "letters.txt");
            List<String> result = new ArrayList<>();
            try {
                List<String> content = Files.readAllLines(file);
                while (i < content.size()) {
                    String line = content.get(i);
                    i++;
                    if (delimiters == index && 0 < line.length() && line.charAt(0) != '_') {
                        result.add(line);
                    } else if (0 < line.length() && line.charAt(0) == '_') {
                        delimiters++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
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

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }
}
