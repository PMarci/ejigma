package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.PlugBoardConfig;
import ejigma.util.ScrambleResult;
import ejigma.util.Util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlugBoard extends Scrambler {

    private static final Random RANDOM = new Random();

    private final String sourceString;

    public PlugBoard() throws ScramblerSettingException {
        this(Scrambler.DEFAULT_ALPHABET_STRING, null, null);
    }

    public PlugBoard(
            String alphabetString,
            String sourceString,
            String wiringString) throws ScramblerSettingException {

        super(alphabetString, wiringString, getPlugBoardType(alphabetString, sourceString, wiringString));
        this.sourceString = sourceString;
        validatePlugBoard();
        setWiring(sourceString, this.wiringString);
    }

    @Override
    public void validateWiringString(String wiringString) throws ScramblerSettingException {
        if (wiringString != null && wiringString.length() > alphabetString.length()) {
            throw new ScramblerSettingException(String.format(
                    "The wiringString %s is longer than the alphabetString %s of this PlugBoard!",
                    wiringString,
                    alphabetString));
        }
        if (wiringString != null && wiringString.chars().anyMatch(c -> alphabetString.indexOf(c) == -1)) {
            throw new ScramblerSettingException(String.format(
                    "The wiringString %s for this PlugBoard contains characters not in the alphabetString %s!",
                    wiringString,
                    alphabetString));
        }
    }

    public void validatePlugBoard() throws ScramblerSettingException {
        validatePlugBoard(
                (sourceString != null) ? sourceString : "",
                (wiringString != null) ? wiringString : "");
    }

    private void validatePlugBoard(String sourceString, String wiringString) throws ScramblerSettingException {
        int[] linksToCheck = setSomePlugs(sourceString, wiringString, noOpLinks(alphabet.length));
        Map<Integer, Character> chainWired = IntStream.range(0, sourceString.length())
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, sourceString.toCharArray()[i]))
                // we're looking for source symbols appearing in the wiring at a different index
                .filter(entry -> {
                    int indexInWiring = wiringString.indexOf(entry.getValue(), entry.getKey() - 1);
                    return indexInWiring != -1 && indexInWiring != entry.getKey();
                })
                // when a source char points to a destination char, the destination char has to point
                // to the source char
                .filter(entry -> {
                            int wiringSource = alphabetString.indexOf(entry.getValue());
                            int wiringDest = linksToCheck[wiringSource];
                            return alphabetString.charAt(wiringDest) != linksToCheck[wiringDest];
                        }
                       )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Do something about the message in situations like PlugBoardTest.testo5
        if (!chainWired.isEmpty()) {
            String chainElements = chainWired.entrySet().stream()
                    .map(entry -> {
                        char wiringChar = wiringString.charAt(entry.getKey());
                        char sourceChar = entry.getValue();
                        return String.format(
                                "Symbol %1$s maps to %2$s, but %2$s maps to %3$s instead of %1$s!",
                                sourceChar,
                                wiringChar,
                                (sourceString.indexOf(wiringChar) == -1) ?
                                wiringChar :
                                wiringString.charAt(sourceString.indexOf(wiringChar)));
                    })
                    .collect(Collectors.joining("\n"));
            throw new ScramblerSettingException(String.format(
                    "The source and wiring strings %s -> %s for this PlugBoard has multiple plugs to a single letter!" +
                            "%n They are as follows: %n%s",
                    sourceString,
                    wiringString,
                    chainElements));
        }
    }

    private static int[] noOpLinks(int len) {
        return IntStream.range(0, len).toArray();
    }

    @Override
    void setWiring(String sourceString, String wiringString) {
        int[] newLinks = noOpLinks(alphabet.length);
        if (sourceString != null && !sourceString.isEmpty() && wiringString != null && !wiringString.isEmpty()) {
            setSomePlugs(sourceString, wiringString, newLinks);
        }
        this.forwardLinks = newLinks;
        this.reverseLinks = this.forwardLinks;
    }

    private int[] setSomePlugs(String sourceString, String wiringString, int[] newLinks) {
        boolean[] touched = new boolean[alphabet.length];
        Arrays.fill(touched, false);
        char[] sourceArray;
        char[] wiringArray;
        if (sourceString != null && wiringString != null) {
            sourceArray = sourceString.toCharArray();
            wiringArray = wiringString.toCharArray();
        } else {
            sourceArray = new char[0];
            wiringArray = new char[0];
        }
        int i = 0;
        int j = 0;
        char sourceChar;
        int sourceCharIndex;
        char wiringChar;
        int wiringCharIndex;
        int lastSwapInd = -1;
        char alphabetChar;
        boolean sourceMatched;
        boolean wiringMatched;

        int lastAlphaIndex = alphabet.length - 1;
        while (i < sourceArray.length) {
            sourceCharIndex = -1;
            wiringCharIndex = -1;
            sourceChar = sourceArray[i];
            wiringChar = wiringArray[i];
            // forwardlinks at alphabetString.indexOf(sourceChar) has to be set to alphabetString.indexOf(wiringchar)
            // forwardlinks at alphabetString.indexOf(wiringchar) has to be set to alphabetString.indexOf(sourcheChar)
            do {
                if (!touched[j]) {
                    alphabetChar = alphabetString.charAt(j);
                    sourceMatched = sourceChar == alphabetChar;
                    wiringMatched = wiringChar == alphabetChar;
                    if (sourceCharIndex == -1 && sourceMatched) {
                        sourceCharIndex = j;
                    }
                    if (wiringCharIndex == -1 && wiringMatched) {
                        wiringCharIndex = j;
                    }
                    if (sourceCharIndex != -1 && wiringCharIndex != -1 && (sourceMatched || wiringMatched)) {
                        newLinks[sourceCharIndex] = wiringCharIndex;
                        newLinks[wiringCharIndex] = sourceCharIndex;
                        touched[sourceCharIndex] = touched[wiringCharIndex] = true;
                        lastSwapInd = (sourceMatched) ? sourceCharIndex : wiringCharIndex;
                    }
                }
                j = (j >= lastAlphaIndex) ? 0 : j + 1;
            } while ((sourceCharIndex == -1 || wiringCharIndex == -1) && j != lastSwapInd);
            i++;
        }
        return newLinks;
    }

    @Override
    protected ScrambleResult scrambleInput(ScrambleResult input, int[] links) {
        int inputPos = input.getResult();
        char wiringInput = alphabet[inputPos];
        int link = links[inputPos];
        char wiringOutput = alphabet[link];
        return input.putResult(link, wiringInput, wiringOutput, wiringOutput, type.getName());
    }

    @Override
    protected char scrambleInput(char input, int[] links) {
        int inputPos = alphabetString.indexOf(input);
        int link = links[inputPos];
        return alphabet[link];
    }


    @Override
    ScrambleResult scramble(ScrambleResult input) {
        return scrambleInput(input, forwardLinks);
    }

    @Override
    char scramble(char input) {
        return scrambleInput(input, forwardLinks);
    }

    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        return scramble(input);
    }

    @Override
    char reverseScramble(char input) {
        return scramble(input);
    }

    // TODO document reset via empty
    public static String[] splitInitString(String alphabetString, String initString) throws ScramblerSettingException {
        String denyStringFormat = "Not a valid initString! Reason: %s";
        String reasonUnequal = "Source and wiring Strings are of unequal length!";
        String reasonInvalidSeparator = "No separator character outside the current alphabet found!";
        String[] result;
        if (!initString.isEmpty()) {
            int splitIndex = IntStream.range(0, initString.length())
                    .filter(i -> alphabetString.indexOf(initString.charAt(i)) == -1)
                    .findFirst()
                    .orElseThrow(() -> new ScramblerSettingException(String.format(denyStringFormat, reasonInvalidSeparator)));
            result = new String[]{initString.substring(0, splitIndex), initString.substring(splitIndex + 1)};
        } else {
            result = new String[]{"", ""};
        }
        if (result[0].length() != result[1].length()) {
            throw new ScramblerSettingException(String.format(denyStringFormat, reasonUnequal));
        }
        return result;
    }

    // TODO test
    // TODO generalize with two other very similar methods
    public static PlugBoardConfig auto(String alphabetString) {
        int aLen = alphabetString.length();
        int noOfPairs = RANDOM.nextInt(aLen);
        int sourceIndex;
        boolean[] sourceDrawn = new boolean[aLen];
        Arrays.fill(sourceDrawn, false);
        StringBuilder sourceBuilder = new StringBuilder();

        if (aLen > 2) {
            for (int i = 0; i < noOfPairs; i++) {
                do {
                    sourceIndex = RANDOM.nextInt(aLen);
                } while (sourceDrawn[sourceIndex]);
                sourceDrawn[sourceIndex] = true;
                sourceBuilder.append(alphabetString.charAt(sourceIndex));
            }
        }
        String source = sourceBuilder.toString();
        String string = Util.generate2Cycles(source);
        return getPlugBoardType(alphabetString, source, string);
    }

    public static PlugBoardConfig getPlugBoardType(String alphabetString,
                                                   String initString) throws ScramblerSettingException {
        String[] initStrings = PlugBoard.splitInitString(alphabetString, initString);
        return getPlugBoardType(alphabetString, initStrings[0], initStrings[1]);
    }

    private static PlugBoardConfig getPlugBoardType(String alphabetString,
                                                    String sourceString,
                                                    String wiringString) {
        return new PlugBoardConfig() {

            @Override
            public String getName() {
                return "PLUGBOARD";
            }

            @Override
            public String getSourceString() {
                return sourceString;
            }

            @Override
            public String getWiringString() {
                return wiringString;
            }

            @Override
            public String getAlphabetString() {
                return alphabetString;
            }

            public String getInitString() {
                return sourceString + '\u0000' + wiringString;
            }

            @Override
            public String toString() {
                return getName();
            }
        };
    }
}
