package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.type.ScramblerType;
import camel.enigma.util.ScrambleResult;

public class PlugBoard extends Scrambler {

    private String sourceString;

    public PlugBoard(String alphabetString, String sourceString, String wiringString)
            throws ScramblerSettingException {
        super(alphabetString, wiringString, getPlugBoardType(alphabetString));
        this.sourceString = sourceString;
        // TODO validate shorter than alphabet, contains only alphabet (or filter)
        setWiring(this.sourceString, this.wiringString);
    }

    public PlugBoard() throws ScramblerSettingException {
        this(Scrambler.DEFAULT_ALPHABET_STRING, null, null);
    }

    @Override
    void validateWiringString(String wiringString) throws ScramblerSettingException {
        // TODO
    }

    @Override
    void setWiring(String sourceString, String wiringString) {
        this.forwardLinks = new int[alphabet.length];
        if (sourceString != null && !sourceString.isEmpty() && wiringString != null && !wiringString.isEmpty()) {
            setSomePlugs(sourceString, wiringString);
        } else {
            setNoPlugs();
        }
        this.reverseLinks = this.forwardLinks;
    }

    private void setNoPlugs() {
        for (int i = 0; i < alphabet.length; i++) {
            forwardLinks[i] = i;
        }
    }

    private void setSomePlugs(String sourceString, String wiringString) {
        markArray(forwardLinks);
        char[] sourceArray = sourceString.toCharArray();
        char[] wiringArray = wiringString.toCharArray();
        int i = 0;
        int j = 0;
        char sourceChar;
        char wiringChar;
        int outputAddress;
        char alphabetChar;
        boolean match = false;
        boolean unmapped;
        boolean lastAlpha;
        boolean lastSource;
        int lastSourceIndex = sourceArray.length - 1;
        int lastAlphaIndex = alphabet.length - 1;
        do {
            sourceChar = sourceArray[i];
            wiringChar = wiringArray[i];
            outputAddress = getAlphabetString().indexOf(wiringChar);
            do {
                unmapped = forwardLinks[j] == j || forwardLinks[j] == -1;
                if (unmapped) {
                    alphabetChar = alphabet[j];
                    match = sourceChar == alphabetChar;
                    forwardLinks[j] = match ? outputAddress : j;
                }
                lastAlpha = j >= lastAlphaIndex;
                lastSource = i >= lastSourceIndex;
                j = (!lastAlpha) ? j + 1 : 0;
            } while (!match && !(lastSource && lastAlpha));
            i = (!lastSource) ? i + 1 : lastSourceIndex;
        } while (!(lastSource && lastAlpha));
    }

    private static void markArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = -1;
        }
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
        int inputPos = this.alphabetString.indexOf(input);
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


    private static ScramblerType<PlugBoard> getPlugBoardType(String alphabetString) {
        return new ScramblerType<PlugBoard>() {
            @Override
            public String getName() {
                return "PLUGBOARD";
            }

            @Override
            public PlugBoard freshScrambler() {
                PlugBoard plugBoard = null;
                try {
                    plugBoard = new PlugBoard();
                } catch (ScramblerSettingException ignored) {
                    // ignored
                }
                return plugBoard;
            }

            @Override
            public String getAlphabetString() {
                return alphabetString;
            }

        };
    }
}
