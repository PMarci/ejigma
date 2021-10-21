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
import java.util.stream.Collectors;

public class ScrambleResult {

    private static boolean blink;

    private static final String INPUT_STRING = "INPUT";
    private static final String OUTPUT_STRING = "OUTPUT";

    private static final String STEP_SEPARATOR = "/";

    // TODO try and get out of here
    private final String alphabetString;
    // TODO try and get out of here
    private final char[] alphabet;

    private int result;
    private char resultAsChar;
    private List<HistoryEntry> history;

    private Character offsetAsChar;
    private static final AttributedStyle redStyle =
            new AttributedStyle().foreground(AttributedStyle.BRIGHT).foreground(AttributedStyle.RED);
    private static final AttributedStyle blackOnWhiteStyle = new AttributedStyle().foreground(AttributedStyle.BLACK)
            .background(AttributedStyle.BRIGHT)
            .background(AttributedStyle.WHITE);
    private static final AttributedStyle whiteOnBlackStyle = new AttributedStyle().foreground(AttributedStyle.BRIGHT)
            .foreground(AttributedStyle.WHITE)
            .background(AttributedStyle.BLACK);

    public ScrambleResult(String alphabetString, Character resultAsChar) {
        this(0, alphabetString, resultAsChar);
    }

    public ScrambleResult(int result, String alphabetString, Character resultAsChar) {
        this.alphabetString = alphabetString;
        this.alphabet = alphabetString.toCharArray();
        history = new ArrayList<>();
        putResult(result, resultAsChar, resultAsChar, resultAsChar, INPUT_STRING);
        putCharInputToIntResult();
    }

    public void putCharInputToIntResult() {
        int wheelPos = this.alphabetString.indexOf(getResultAsChar());
        setResult(wheelPos);
    }

    public char getResultAsChar() {
        return resultAsChar;
    }

    public ScrambleResult putResult(
            int result,
            char wiringInput,
            char wiringOutput,
            char resultAsChar,
            String stationId,
            int offset,
            char offsetAsChar) {

        this.result = result;
        this.resultAsChar = resultAsChar;
        this.offsetAsChar = offsetAsChar;
        addHistoryEntry(wiringInput, wiringOutput, stationId, offset, offsetAsChar);
        return this;
    }

    public ScrambleResult putResult(
            int result,
            char wiringInput,
            char wiringOutput,
            char resultAsChar,
            String stationId) {

        this.result = result;
        this.resultAsChar = resultAsChar;
        addHistoryEntry(wiringInput, wiringOutput, stationId);
        return this;
    }

    private void addHistoryEntry(char wiringInput,
                                 char wiringOutput,
                                 String stationId,
                                 Integer offset,
                                 Character offsetAsChar) {
        HistoryEntry newEntry = generateHistoryEntry(wiringInput, wiringOutput, stationId);
        newEntry.setOffset(offset);
        newEntry.setOffsetAsChar(offsetAsChar);
        history.add(newEntry);
    }

    private void addHistoryEntry(char wiringInput, char wiringOutput, String stationId) {
        HistoryEntry newEntry = generateHistoryEntry(wiringInput, wiringOutput, stationId);
        history.add(newEntry);
    }

    private HistoryEntry generateHistoryEntry(char wiringInput, char wiringOutput, String stationId) {
        int maxPassNo = history.stream().sequential()
                .filter(historyEntry -> historyEntry.getStationId().equals(stationId))
                .map(HistoryEntry::getPassNo)
                .max(Integer::compareTo).orElse(0);
        return new HistoryEntry(wiringInput, wiringOutput, result, stationId, alphabet, ++maxPassNo);
    }

    public void recordOutput() {
        HistoryEntry last = history.get(history.size() - 1);
        Character lastOutput = last.getWiringOutput();
        // assuming no rotation since last...
        Integer lastOffset = last.getOffset();
        char wiringOutput = alphabet[result];
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
        List<String> resultList = new ArrayList<>();
        int historySize = history.size();
        int lastOutputIndex = (historySize > 0) ?
                              alphabetString.indexOf(history.get(historySize - 1).getWiringOutput()) :
                              0;
        List<String> letterLines = Collections.emptyList();
        if (-1 < lastOutputIndex && lastOutputIndex < HistoryEntry.letters.length) {
            letterLines = HistoryEntry.letters[lastOutputIndex];
        }
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

    private void setResult(int result) {
        this.result = result;
    }

    public static class HistoryEntry {

        private static final List<String>[] letters = initLetters();

        private static final int HEIGHT = 15;

        private static final int PADDING = 20;
        private static final int SECOND_PADDING = 60;
        private char wiringInput;

        private char wiringOutput;
        private int result;
        private String stationId;
        private final int passNo;
        private final char[] alphabet;

        private Integer offset;
        private Character offsetAsChar;

        HistoryEntry(char wiringInput,
                     char wiringOutput,
                     int result,
                     String stationId,
                     char[] alphabet,
                     int passNo) {
            this.wiringInput = wiringInput;
            this.wiringOutput = wiringOutput;
            this.result = result;
            this.stationId = stationId;
            this.alphabet = alphabet;
            this.passNo = passNo;
        }

        HistoryEntry(char wiringInput, char wiringOutput, int result, String stationId, char[] alphabet) {
            this.wiringInput = wiringInput;
            this.wiringOutput = wiringOutput;
            this.result = result;
            this.stationId = stationId;
            this.alphabet = alphabet;
            passNo = 1;
        }

        @Override
        public String toString() {
            int firstMidLast = getFirstMidLast();
            String firstLine = getWiringFirstLine(firstMidLast);
            String secondLine = getWiringSecondLine(firstMidLast);
            return String.join("\n", firstLine, secondLine);
        }

        List<String> toDetailString(String letterLine1, String letterLine2) {
            List<String> resultList = new ArrayList<>();
            int firstMidLast = getFirstMidLast();
            String firstLine = getWiringFirstLine(letterLine1, firstMidLast);
            resultList.add(firstLine);
            String secondLine = getWiringSecondLine(letterLine2, firstMidLast);
            resultList.add(secondLine);
            return resultList;
        }

        private String getWiringFirstLine(int firstMidLast) {
            return getWiringFirstLine(null, firstMidLast);
        }

        private String getWiringFirstLine(String letterLine1, int firstMidLast) {
            StringBuilder firstLine = new StringBuilder();
            firstLine.append(pad(getStepIdString())).append(" : ");
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
            if (firstMidLast == -1) {
                wiringThirdPart = " ═╗";
            } else if (firstMidLast == 1) {
                wiringThirdPart = String.valueOf(wiringOutput);
            } else {
                wiringThirdPart = String.valueOf(wiringOutput) + " ═╗";
            }
            firstLine.append(wiringFirstPart)
                .append(wiringSecondPart)
                .append(wiringThirdPart);

            int firstLineLength = firstLine.length();
            if (letterLine1 != null) {
                firstLine.append(getPadding(firstLineLength, SECOND_PADDING)).append(letterLine1);
            }
            return firstLine.toString();
        }

        private String getWiringSecondLine(int firstMidLast) {
            return getWiringSecondLine(null, firstMidLast);
        }

        private String getWiringSecondLine(String letterLine2, int firstMidLast) {
            StringBuilder secondLine = new StringBuilder();
            if (firstMidLast != 1) {
                secondLine.append(getPadding("")).append("   ╔════[").append(alphabet[result]).append("]═════╝");
            }
            if (getOffsetAsChar() != null) {
                secondLine.append(", offset = ").append(offsetAsChar);
            }
            int secondLineLength = secondLine.length();
            if (letterLine2 != null) {
                secondLine.append(getPadding(secondLineLength, SECOND_PADDING)).append(letterLine2);
            }
            return secondLine.toString();
        }

        private int getFirstMidLast() {
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
            return firstMidLast;
        }

        private String getStepIdString() {
            return (passNo > 1) ? stationId + STEP_SEPARATOR + passNo : stationId;
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

        private static List<String>[] initLetters() {
            // TODO read size first
            List<String>[] result = new ArrayList[28];
            for (int i = 0; i < 28; i++) {
                result[i] = loadLetter(i, "letters.txt");
            }
            return result;
        }

        public static List<String> loadLetter(int index, String fileName) {

            char letterSeparator = '_';
            int delimiters = 0;
            int i = 0;
            List<String> content = Collections.emptyList();
            try (InputStream lettersStream = ScrambleResult.class.getResourceAsStream(fileName)) {
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
                if (delimiters == index && 0 < line.length() && line.charAt(0) != letterSeparator) {
                    result.add(line);
                } else if (0 < line.length() && line.charAt(0) == letterSeparator) {
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

        public int getPassNo() {
            return passNo;
        }
    }
}
