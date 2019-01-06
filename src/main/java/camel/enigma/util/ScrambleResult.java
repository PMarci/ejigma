package camel.enigma.util;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScrambleResult {

    private static boolean blink;

    private static final String INPUT_STRING = "INPUT";
    private static final String OUTPUT_STRING = "OUTPUT";

    private static final String STEP_SEPARATOR = "/";
    private static final String STEP_REGEX = "^([^/]+)(?:" + STEP_SEPARATOR + "(\\d+))?$";
    private static final Pattern STEP_PATTERN = Pattern.compile(STEP_REGEX);

    private final String alphabetString;
    private final char[] alphabet;

    private int result;
    private char resultAsChar;
    private List<HistoryEntry> history;

    private Character offsetAsChar;

    public ScrambleResult(String alphabetString, Character resultAsChar) {
        this.alphabetString = alphabetString;
        this.alphabet = alphabetString.toCharArray();
        history = new ArrayList<>();
        putResult(0, resultAsChar, resultAsChar, resultAsChar, INPUT_STRING);
        putCharInputToIntResult(this);
    }

    public void putCharInputToIntResult() {
        putCharInputToIntResult(this);
    }

    private void putCharInputToIntResult(ScrambleResult input) {
        int wheelPos = this.alphabetString.indexOf(input.getResultAsChar());
        input.setResult(wheelPos);
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
        String newStepId;
        if (!stepVisited) {
            newStepId = stepId;
        } else if (passNo == 0) {
            newStepId = stepId + STEP_SEPARATOR + 2;
        } else {
            newStepId = stationId + STEP_SEPARATOR + ++passNo;
        }
        newEntry = new HistoryEntry(wiringInput, wiringOutput, result, newStepId, alphabet);
        return newEntry;
    }

    public void recordOutput() {
        HistoryEntry last = history.get(history.size() - 1);
        Character lastOutput = last.getWiringOutput();
        // assuming no rotation since last...
        // TODO figure out association with keyboard/output
        Integer lastOffset = last.getOffset();
        ////////
        char wiringOutput = alphabet[result];
        ////////
        HistoryEntry newEntry = new HistoryEntry(
                lastOutput,
                wiringOutput,
                result,
                OUTPUT_STRING,
                alphabet);
        newEntry.setOffset(lastOffset);
        this.resultAsChar = wiringOutput;
        history.add(newEntry);
    }

    public List<String> printHistory() {
        AttributedStyle redStyle = new AttributedStyle().foreground(AttributedStyle.BRIGHT).foreground(AttributedStyle.RED);
        AttributedStyle blackOnWhiteStyle = new AttributedStyle().foreground(AttributedStyle.BLACK).background(AttributedStyle.BRIGHT).background(AttributedStyle.WHITE);
        AttributedStyle whiteOnBlackStyle = new AttributedStyle().foreground(AttributedStyle.BRIGHT).foreground(AttributedStyle.WHITE).background(AttributedStyle.BLACK);
        List<String> resultList = new ArrayList<>();
        int historySize = history.size();
        int lastOutputIndex = (historySize > 0) ?
                              alphabetString.indexOf(history.get(historySize - 1).getWiringOutput()) :
                              0;
        List<String> letterLines = HistoryEntry.letters[lastOutputIndex];
        Iterator<String> letterLineIterator = letterLines.iterator();
        for (int i = 0; fitsInHeight(historySize, i); i++) {
            HistoryEntry historyEntry;
            List<String> historyEntryBlock;
            String letterLine1 = null;
            String letterLine2 = null;
            if (i < historySize) {
                historyEntry = history.get(i);
                if (letterLineIterator.hasNext()) {
                    letterLine1 = new AttributedString(letterLineIterator.next(), redStyle).toAnsi();
                }
                if (letterLineIterator.hasNext()) {
                    letterLine2 = new AttributedString(letterLineIterator.next(), redStyle).toAnsi();
                }
                historyEntryBlock = historyEntry.toDetailString(letterLine1, letterLine2);
                resultList.addAll(historyEntryBlock);
            }
            if (!fitsInHeight(historySize, i + 1)) {
                if (blink()) {
                    resultList.add(new AttributedString("SUP", whiteOnBlackStyle).toAnsi());
                } else {
                    resultList.add(new AttributedString("SUP", blackOnWhiteStyle).toAnsi());
                }
            }
        }
        return resultList;
    }

    private static boolean blink() {
        blink = !blink;
        return blink;
    }

    private boolean fitsInHeight(int historySize, int i) {
        return i < historySize || i < HistoryEntry.HEIGHT / 2;
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
        private final char[] alphabet;

        private Integer offset;
        private Character offsetAsChar;

        HistoryEntry(char wiringInput, char wiringOutput, int result, String stationId, char[] alphabet) {
            this.wiringInput = wiringInput;
            this.wiringOutput = wiringOutput;
            this.result = result;
            this.stationId = stationId;
            this.alphabet = alphabet;
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
            String wiringThirdPart =
                    (terminal == 1) ? " :::::> " + wiringOutput : String.valueOf(wiringOutput) + " ═╗\n";
            String returnBranch = getPadding("") + "   ╔════[" + alphabet[result] + "]═════╝";
            String wiringPart =
                    wiringFirstPart + wiringSecondPart + wiringThirdPart + ((terminal != 1) ? returnBranch : "");
            String mainPart = pad(stationId) + " : " + wiringPart;
            return ((getOffsetAsChar() != null) ? mainPart + ", offset = " + offsetAsChar : mainPart);
        }

        List<String> toDetailString(String letterLine1, String letterLine2) {
            StringBuilder firstLine = new StringBuilder();
            StringBuilder secondLine = new StringBuilder();
            List<String> resultList = new ArrayList<>();
            int firstMidLast;
            if (stationId.equals(INPUT_STRING)) {
                firstMidLast = -1;
            } else {
                if (stationId.equals(OUTPUT_STRING)) {
                    firstMidLast = 1;
                } else {
                    firstMidLast = 0;
                }
            }
            firstLine.append(pad(stationId)).append(" : ");
            String wiringFirstPart;
            if (firstMidLast == -1) {
                wiringFirstPart = "   " + wiringInput + " " + ":::::>";
            } else if (firstMidLast == 1) {
                wiringFirstPart = "╚► " + ":::::> ";
            } else {
                wiringFirstPart = "╚► " + wiringInput;
            }
            String wiringSecondPart = (firstMidLast == 0) ? " :::> " : "";
            String wiringThirdPart;
            if (firstMidLast == 1) {
                wiringThirdPart = String.valueOf(wiringOutput);
            } else if (firstMidLast == -1) {
                wiringThirdPart = " ═╗";
            } else {
                wiringThirdPart = String.valueOf(wiringOutput) + " ═╗";
            }
            String returnLink = getPadding("") + "   ╔════[" + alphabet[result] + "]═════╝";
            firstLine.append(wiringFirstPart)
                    .append(wiringSecondPart)
                    .append(wiringThirdPart);
            int firstLineLength = firstLine.length();
            if (letterLine1 != null) {
                firstLine.append(getPadding(firstLineLength, SECOND_PADDING)).append(letterLine1);
            }
            resultList.add(firstLine.toString());
            if (firstMidLast != 1) {
                secondLine.append(returnLink);
            }
            if (getOffsetAsChar() != null) {
                secondLine.append(", offset = ").append(offsetAsChar);
            }
            int secondLineLength = secondLine.length();
            if (letterLine2 != null) {
                secondLine.append(getPadding(secondLineLength, SECOND_PADDING)).append(letterLine2);
            }
            resultList.add(secondLine.toString());
            return resultList;
        }

        static String getPadding(String s) {
            return getPadding(s, PADDING);
        }

        public static String getPadding(String s, int padding) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getSpace(s, padding); i++) {
                sb.append(' ');
            }
            return sb.toString();
        }

        static String getPadding(int strLen, int padding) {
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

        static String pad(String s) {
            return s.concat(getPadding(s, HistoryEntry.PADDING));
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
            List<String> content = Collections.emptyList();
            try (InputStream lettersStream = ScrambleResult.class.getResourceAsStream("letters.txt")) {
                content = new BufferedReader(new InputStreamReader(lettersStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> result = new ArrayList<>();
            while (i < content.size()) {
                String line = content.get(i);
                i++;
                if (delimiters == index && 0 < line.length() && line.charAt(0) != '_') {
                    result.add(line);
                } else if (0 < line.length() && line.charAt(0) == '_') {
                    delimiters++;
                }
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
