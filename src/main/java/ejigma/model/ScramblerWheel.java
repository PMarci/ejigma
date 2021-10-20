package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.ScramblerType;
import ejigma.util.ScrambleResult;

public abstract class ScramblerWheel extends Scrambler {

    private int offset;
    char offsetAsChar;
    private final boolean staticc;

    ScramblerWheel(
            String alphabetString,
            String wiringString,
            boolean staticc,
            ScramblerType<? extends ScramblerWheel> scramblerType)
        throws ScramblerSettingException {

        super(alphabetString, wiringString, scramblerType);
        setWiring(getAlphabetString(), this.wiringString);
        offset = 0;
        offsetAsChar = alphabet[offset];
        this.staticc = staticc;
    }

    protected abstract boolean isNotchEngaged();

    @Override
    protected ScrambleResult scrambleInput(ScrambleResult input, int[] links) {
        int inputPos = input.getResult();
        int wrappedOffsetPos = (inputPos + offset) % alphabet.length;
        char wiringInput = alphabet[wrappedOffsetPos];
        int link = links[wrappedOffsetPos];
        char wiringOutput = alphabet[link];
        int outputPos = (link - offset + alphabet.length) % alphabet.length;
        return input.putResult(outputPos, wiringInput, wiringOutput, wiringOutput, type.getName(), offset, offsetAsChar);
    }

    @Override
    protected char scrambleInput(char input, int[] links) {
        int inputPos = alphabetString.indexOf(input);
        int wrappedOffsetPos = (inputPos + offset) % alphabet.length;
        char wiringInput = alphabet[wrappedOffsetPos];
        int link = links[alphabetString.indexOf(wiringInput)];

        return alphabet[(link - offset + alphabet.length ) % alphabet.length];
    }

    boolean click() {
        boolean result = false;
        if (!staticc) {
            result = isNotchEngaged();
            offset = (offset == alphabet.length - 1) ? 0 : offset + 1;
            offsetAsChar = alphabet[offset];
        }
        return result;
    }

    public int getOffset() {
        return offset;
    }

    public char getOffsetAsChar() {
        return offsetAsChar;
    }

    public void setOffset(char offset) {
        int index = getAlphabetString().indexOf(offset);
        if (index != -1) {
            this.offsetAsChar = offset;
            this.offset = index;
        } else {
            throw new IllegalArgumentException("invalid offset!");
        }
    }

    public void setOffset(int offset) {
        if (-1 < offset && offset < alphabet.length) {
            this.offset = offset;
            this.offsetAsChar = getAlphabetString().charAt(offset);
        } else {
            throw new IllegalArgumentException("invalid offset!");
        }
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
        return scrambleInput(input, reverseLinks);
    }

    @Override
    char reverseScramble(char input) {
        return scrambleInput(input, reverseLinks);
    }

    @Override
    void setWiring(String alphabetString, String wiringString) {
        this.forwardLinks = new int[alphabetString.length()];
        this.reverseLinks = new int[alphabetString.length()];
        char[] alphabetArray = alphabetString.toCharArray();
        char[] wiringArray = wiringString.toCharArray();
        for (int i = 0, alphabetLength = alphabetArray.length; i < alphabetLength; i++) {
            char target = wiringArray[i];
            char source = alphabetArray[i];
            this.forwardLinks[i] = alphabetString.indexOf(target);
            this.reverseLinks[i] = wiringString.indexOf(source);
        }

    }
}
