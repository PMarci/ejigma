package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.Util;

public class Rotor extends ScramblerWheel {

    static final char[] DEFAULT_NOTCH = new char[] { 'Q' };

    private int ringSetting;
    private char ringSettingAsChar;
    private char[] notch;

    // TODO remove tests and const if useless
    Rotor(String wiringString) throws ScramblerSettingException {
        this(DEFAULT_ALPHABET_STRING, wiringString, DEFAULT_NOTCH, false, RotorType.NOOP);
    }

    Rotor(String wiringString, char[] notch, ScramblerType scramblerType) throws ScramblerSettingException{
        this(DEFAULT_ALPHABET_STRING, wiringString, notch, false, scramblerType);
    }

    Rotor(
        String alphabetString,
        String wiringString,
        char[] notch,
        boolean staticc,
        ScramblerType scramblerType) throws ScramblerSettingException {

        super(alphabetString, wiringString, staticc, scramblerType);
        this.notch = notch;
    }

    // TODO replace
    public static char subtractOffset(char[] alphabet, char inputPos, int offset) {
        int indexOfInput = Util.indexOf(alphabet, inputPos);
        int offsetKeyIndex = (indexOfInput - offset + alphabet.length) % alphabet.length;
        return alphabet[offsetKeyIndex];
    }

    // TODO update
//    void setRing(int ringSetting) {
//        for (int i = 0, wiringsLength = wirings.length; i < wiringsLength; i++) {
//            Wiring wiring = wirings[i];
//            int offsetPos = (i + ringSetting) % wiringsLength;
//            Wiring offsetWiring = wirings[offsetPos];
//            wiring.setTarget(offsetWiring.getTarget());
//        }
//    }

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

    @Override
    public String toString() {
        return super.toString();
    }

    public boolean isNotchEngaged() {
        boolean result = false;
        if (notch != null) {
            result = Util.containsChar(notch, offsetAsChar);
        }
        return result;
    }

    public char[] getNotch() {
        return notch;
    }

    public void setNotch(char[] notch) {
        this.notch = notch;
    }
}
