package ejigma.model;

import ejigma.exception.ScramblerSettingAlphabetException;
import ejigma.exception.ScramblerSettingException;
import ejigma.exception.ScramblerSettingLengthException;
import ejigma.exception.ScramblerSettingWiringException;
import ejigma.model.type.Printable;
import ejigma.model.type.ScramblerType;
import ejigma.util.GsonExclude;
import ejigma.util.ScrambleResult;
import ejigma.util.Util;

import static java.util.Objects.requireNonNull;

public abstract class Scrambler<S extends Scrambler<S, T>, T extends ScramblerType<S, T>> implements Printable {

    public static final String DEFAULT_ALPHABET_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final char[] DEFAULT_ALPHABET = DEFAULT_ALPHABET_STRING.toCharArray();

    // trying something out for gson
    protected T type;
    protected final String alphabetString;
    protected final String wiringString;

    @GsonExclude
    protected final char[] alphabet;

    @GsonExclude
    int[] forwardLinks;

    @GsonExclude
    int[] reverseLinks;

    protected Scrambler(String alphabetString, String wiringString, T scramblerType) throws ScramblerSettingException {
        this.type = scramblerType;
        validateAlphabetString(alphabetString);
        this.alphabetString = alphabetString;
        this.alphabet = this.alphabetString.toCharArray();
        validateWiringString(wiringString);
        this.wiringString = wiringString;
    }

    abstract void setWiring(String alphabetString, String wiringString);

    protected abstract ScrambleResult scrambleInput(ScrambleResult input, int[] links);

    protected abstract char scrambleInput(char input, int[] links);

    abstract ScrambleResult scramble(ScrambleResult input);

    abstract char scramble(char input);

    abstract ScrambleResult reverseScramble(ScrambleResult input);

    abstract char reverseScramble(char input);

    private void validateAlphabetString(String alphabetString) throws ScramblerSettingAlphabetException {
        requireNonNull(alphabetString);
        for (char c : alphabetString.toCharArray()) {
            int freq = Util.countOccurrences(alphabetString, c);
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
            int freq = Util.countOccurrences(wiringString, c);
            if (freq > 1) {
                throw new ScramblerSettingWiringException("Scrambler wirings can only map each letter once!");
            }
        }
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    public T getType() {
        return type;
    }

    @Override
    public String toString() {
        return print();
    }
}
