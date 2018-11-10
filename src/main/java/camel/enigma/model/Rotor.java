package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;

public class Rotor extends Scrambler {

    private static final char[] DEFAULT_NOTCH = new char[]{'Q'};

    private int ringSetting;
    private char ringSettingAsChar;
    private char[] notch;
    private int offset;
    private char offsetAsChar;
    private RotorType rotorType;

    private Rotor() throws ScramblerSettingException {
        super();
        offset = 0;
        offsetAsChar = alphabet[offset];
        notch = DEFAULT_NOTCH;
    }

    Rotor(RotorType rotorType) throws ScramblerSettingException {
        this();
        this.rotorType = rotorType;
    }

    public Rotor(String wiringString) throws ScramblerSettingException {
        super(wiringString);
        offset = 0;
        offsetAsChar = alphabet[offset];
        notch = DEFAULT_NOTCH;
    }

    public Rotor(String wiringString, char[] notch, RotorType rotorType) throws ScramblerSettingException {
        this(wiringString);
        this.notch = notch;
        this.rotorType = rotorType;
    }

    public Rotor(String alphabetString, String wiringString) throws ScramblerSettingException {
        super(alphabetString, wiringString);
        offset = 0;
        offsetAsChar = alphabet[offset];
        notch = DEFAULT_NOTCH;
    }

    public Rotor(String alphabetString,
                 String wiringString,
                 char[] notch,
                 RotorType rotorType) throws ScramblerSettingException {
        this(alphabetString, wiringString);
        this.notch = notch;
        this.rotorType = rotorType;
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        char inputPos = input.getResult();
        char key = addOffset(inputPos);
        char value = get(key);
        char outputPos = subtractOffset(value);
        input.putResult(outputPos, rotorType.name(), key, value, offset, offsetAsChar);
        return input;
    }

    private char addOffset(char inputPos) {
        return addOffset(alphabet, inputPos, offset);
    }

    public static char addOffset(char[] alphabet, char inputPos, int offset) {
        int indexOfInput = Util.indexOf(alphabet, inputPos);
        int offsetKeyIndex = (indexOfInput + offset) % alphabet.length;
        return alphabet[offsetKeyIndex];
    }

    private char subtractOffset(char inputPos) {
        return subtractOffset(alphabet, inputPos, offset);
    }

    public static char subtractOffset(char[] alphabet, char inputPos, int offset) {
        int indexOfInput = Util.indexOf(alphabet, inputPos);
        int offsetKeyIndex = (indexOfInput - offset + alphabet.length) % alphabet.length;
        return alphabet[offsetKeyIndex];
    }

    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        char inputPos = input.getResult();
        char key = addOffset(inputPos);
        // TODO real impl
        char wiringSource = 0;
        for (Wiring wiring : wirings) {
            if (key == wiring.getTarget()) {
                wiringSource = wiring.getSource();
                break;
            }
        }
        char outputPos = subtractOffset(wiringSource);
        input.putResult(outputPos, rotorType.name(), key, wiringSource, offset, offsetAsChar);
        return input;
    }

    boolean click() {
        boolean notchEngaged = isNotchEngaged();
        offset = (offset == alphabet.length - 1) ? 0 : offset + 1;
        offsetAsChar = alphabet[offset];
        return notchEngaged;
    }

    public boolean isNotchEngaged() {
        boolean result = false;
        if (notch != null) {
            result = Util.containsChar(notch, offsetAsChar);
        }
        return result;
    }

    // TODO develop better internal rotor state
    void setRing(int ringSetting) {
        for (int i = 0, wiringsLength = wirings.length; i < wiringsLength; i++) {
            Wiring wiring = wirings[i];
            int offsetPos = (i + ringSetting) % wiringsLength;
            Wiring offsetWiring = wirings[offsetPos];
            wiring.setTarget(offsetWiring.getTarget());
        }
    }

    public int getRingSetting() {
        return ringSetting;
    }

    public void setRingSetting(int ringSetting) {
        this.ringSetting = ringSetting;
    }

    public char getRingSettingAsChar() {
        return ringSettingAsChar;
    }

    public void setRingSettingAsChar(char ringSettingAsChar) {
        this.ringSettingAsChar = ringSettingAsChar;
    }

    public RotorType getRotorType() {
        return rotorType;
    }

    public void setRotorType(RotorType rotorType) {
        this.rotorType = rotorType;
    }

    public char getOffsetAsChar() {
        return offsetAsChar;
    }

    public void setOffsetAsChar(char offsetAsChar) {
        this.offsetAsChar = offsetAsChar;
    }

    public char[] getNotch() {
        return notch;
    }

    public void setNotch(char[] notch) {
        this.notch = notch;
    }

    public void setOffset(char offset) {
        this.offsetAsChar = offset;
        int index = Util.indexOf(alphabet, offset);
        if (index != -1) {
            this.offset = index;
        } else {
            throw new IllegalArgumentException("invalid offset!");
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
