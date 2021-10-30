package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.RotorType;
import ejigma.util.Util;

import java.util.stream.IntStream;

public class Rotor extends ScramblerWheel<Rotor, RotorType> {

    private int ringSetting;
    private char ringSettingAsChar;
    private char[] notch;

    public Rotor(
            String alphabetString,
            String wiringString,
            char[] notch,
            boolean staticc,
            RotorType rotorType) throws ScramblerSettingException {

        super(alphabetString, wiringString, staticc, rotorType);
        if (notchValid(notch)) {
            this.notch = notch;
        } else {
            this.notch = new char[]{alphabet[alphabet.length - 1]};
        }
    }

    private boolean notchValid(char[] notch) {
        return IntStream.range(0, notch.length)
                .mapToObj(i -> notch[i])
                .allMatch(c -> Util.containsChar(alphabet, c));
    }

    // TODO update
    void setRing(int ringSetting) {
//        for (int i = 0, wiringsLength = wirings.length; i < wiringsLength; i++) {
//            Wiring wiring = wirings[i];
//            int offsetPos = (i + ringSetting) % wiringsLength;
//            Wiring offsetWiring = wirings[offsetPos];
//            wiring.setTarget(offsetWiring.getTarget());
//        }
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
