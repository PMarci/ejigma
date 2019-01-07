package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.type.ScramblerType;
import camel.enigma.util.ScrambleResult;

public abstract class ScramblerWheel extends Scrambler {

    private int offset;
    char offsetAsChar;
    private final boolean staticc;

    ScramblerWheel(String alphabetString, String wiringString, boolean staticc, ScramblerType scramblerType)
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
        this.offsetAsChar = offset;
        int index = getAlphabetString().indexOf(offset);
        if (index != -1) {
            this.offset = index;
        } else {
            throw new IllegalArgumentException("invalid offset!");
        }
    }

    public void setOffset(int offset) {
        this.offset = offset;
        int index = getAlphabetString().indexOf(offset);
        if (index != -1) {
            this.offset = index;
        } else {
            throw new IllegalArgumentException("invalid offset!");
        }
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        return scrambleInput(input, forwardLinks);
    }

    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
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
            int outputAddress = alphabetString.indexOf(target);
            int inputAddress = wiringString.indexOf(source);
            this.forwardLinks[i] = outputAddress;
            this.reverseLinks[i] = inputAddress;
        }

    }
}
