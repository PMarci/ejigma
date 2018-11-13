package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingAlphabetException;
import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.exception.ScramblerSettingLengthException;
import camel.enigma.exception.ScramblerSettingWiringException;
import camel.enigma.util.ScrambleResult;

import static java.util.Objects.requireNonNull;

public abstract class Scrambler {

    public static final String DEFAULT_ALPHABET_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final char[] DEFAULT_ALPHABET = DEFAULT_ALPHABET_STRING.toCharArray();
    final ScramblerType type;
    final String alphabetString;

    String wiringString;

    final char[] alphabet;
    int[] forwardLinks;
    int[] reverseLinks;

    Scrambler(String alphabetString, String wiringString, ScramblerType scramblerType) throws ScramblerSettingException {
        this.type = scramblerType;
        validateAlphabetString(alphabetString);
        this.alphabetString = alphabetString;
        this.alphabet = this.alphabetString.toCharArray();
        validateWiringString(wiringString);
        this.wiringString = wiringString;
    }

    void initWiring() {
        setWiring(alphabetString, wiringString);
    }

    abstract void setWiring(String alphabetString, String wiringString);

    protected abstract ScrambleResult scrambleInput(ScrambleResult input, int[] links);

    abstract ScrambleResult scramble(ScrambleResult input);

    abstract ScrambleResult reverseScramble(ScrambleResult input);

    private void validateAlphabetString(String alphabetString) throws ScramblerSettingAlphabetException {
        requireNonNull(alphabetString);
        for (char c : alphabetString.toCharArray()) {
            int freq = countOccurrences(alphabetString, c);
            if (freq > 1) {
                throw new ScramblerSettingAlphabetException("An alphabet string can only contain each letter once!");
            }

        }
    }

    void validateWiringString(String wiringString) throws ScramblerSettingException {
        if (wiringString.length() != alphabet.length) {
            throw new ScramblerSettingLengthException(String.format("Wirings only accept %d char strings!", alphabet.length));
        }
        for (char c : alphabet) {
            int freq = countOccurrences(wiringString, c);
            if (freq > 1) {
                throw new ScramblerSettingWiringException("Scrambler wirings can only map each letter once!");
            }
        }
    }

    private int countOccurrences(String s, char inputChar) {
        int result = 0;
        for (char c : s.toCharArray()) {
            if (c == inputChar) {
                result++;
            }
        }
        return result;
    }
}
