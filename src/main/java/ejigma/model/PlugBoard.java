package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.ScramblerType;
import ejigma.util.ScrambleResult;

import java.util.Arrays;

public class PlugBoard extends Scrambler {

    public PlugBoard(String alphabetString, String sourceString, String wiringString)
            throws ScramblerSettingException {
        super(alphabetString, wiringString, getPlugBoardType(alphabetString));
        setWiring(sourceString, this.wiringString);
    }

    public PlugBoard() throws ScramblerSettingException {
        this(Scrambler.DEFAULT_ALPHABET_STRING, null, null);
    }

    @Override
    void validateWiringString(String wiringString) throws ScramblerSettingException {
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

    @Override
    void setWiring(String sourceString, String wiringString) {
        this.forwardLinks = new int[alphabet.length];
        if (sourceString != null && !sourceString.isEmpty()
                && wiringString != null && !wiringString.isEmpty()) {
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
        Arrays.fill(forwardLinks, -1);
        char[] sourceArray = sourceString.toCharArray();
        char[] wiringArray = wiringString.toCharArray();
        int i = 0;
        int j = 0;
        char sourceChar;
        char wiringChar;
        char alphabetChar;
        boolean match = false;
        boolean lastAlpha;
        boolean lastSource;
        int lastSourceIndex = sourceArray.length - 1;
        int lastAlphaIndex = alphabet.length - 1;
        // i goes from 0 to sourceArray.length - 1 and stays there
        do {
            // wiring source and destination chars
            sourceChar = sourceArray[i];
            wiringChar = wiringArray[i];
            // j goes from 0 to alphabet.length - 1 and wraps around to 0
            do {
                // if the current link is either unplugged or not yet touched
                if (forwardLinks[j] == j || forwardLinks[j] == -1) {
                    alphabetChar = alphabet[j];
                    // j is at the alphabet index of the sourceChar
                    match = sourceChar == alphabetChar;
                    // link with the wiringChar's alphabet index
                    forwardLinks[j] = match ? getAlphabetString().indexOf(wiringChar) : j;
                }
                // are we at the end of either array?
                lastAlpha = j >= lastAlphaIndex;
                lastSource = i >= lastSourceIndex;
                // j goes around
                j = (lastAlpha) ? 0 : j + 1;
                // repeat as long there was no match and we're not at the end of both
                // break if there was either a match or we're at the end of both lists
                //  match | lastSource | lastAlpha | result
                //  false |    false   |   false   | true
                //  false |    false   |   true    | true
                //  false |    true    |   true    | false
                //  true  |    false   |   false   | false
                //  true  |    true    |   false   | false
                //  true  |    true    |   true    | false
                //  true  |    false   |   true    | false
                //  false |    true    |   false   | false
            } while (!match && !(lastSource && lastAlpha));
            // i stays at the last index once reached
            i = (lastSource) ? lastSourceIndex : i + 1;
            // break either lastSource or both arrays are done
            //  lastSource | lastAlpha | result
            //     false   |   false   | true
            //     false   |   true    | true
            //     true    |   false   | false
            //     true    |   true    | false
        } while (!(lastSource && lastAlpha));
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


    private static ScramblerType<PlugBoard> getPlugBoardType(String alphabetString) {
        return new ScramblerType<>() {
            @Override
            public String getName() {
                return "PLUGBOARD";
            }

            @Override
            public PlugBoard freshScrambler() {
                PlugBoard plugBoard = null;
                try {
                    plugBoard = new PlugBoard();
                } catch (ScramblerSettingException e) {
                    e.printStackTrace();
                }
                return plugBoard;
            }

            @Override
            public String getAlphabetString() {
                return alphabetString;
            }

            @Override
            public String toString() {
                return getName();
            }
        };
    }
}
