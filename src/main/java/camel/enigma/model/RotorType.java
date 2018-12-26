
package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum RotorType implements ScramblerType<Rotor> {

    // ABCDEFGHIJKLMNOPQRSTUVWXYZ
    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ", new char[]{'Q'}),
    //  ABCDEFGHIJKLMNOPQRSTUVWXYZ
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE", new char[]{'E'}),
    //   ABCDEFGHIJKLMNOPQRSTUVWXYZ
    III("BDFHJLCPRTXVZNYEIWGAKMUSQO", new char[]{'V'}),
    IV("ESOVPZJAYQUIRHXLNFTGKDCMWB", new char[]{'J'}),
    V("VZBRGITYUPSDNHLXAWMJQOFECK", new char[]{'Z'}),
    VI("JPGVOUMFYQBENHZRDKASXLICTW", new char[]{'M', 'Z'}),
    VII("NZJHGRCXMYSWBOUFAIVLPEKQDT", new char[]{'M', 'Z'}),
    VIII("FKQHTLXOCBJSPDZRAMEWNIUYGV", new char[]{'M', 'Z'}),
    // for testing
    NOOP(),
    ERROR1("ABCD", new char[]{'A'}),
    ERROR2("ABC", "ABCD", new char[]{'A'}),
    NOERROR("ABC", "ABC", new char[]{'A'});

    private Rotor rotor;
    private final String alphabetString;
    private final String wiringString;
    private final char[] notch;
    private final boolean staticc;

    RotorType() {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.notch = Rotor.DEFAULT_NOTCH;
        this.staticc = false;
        try {
            freshScrambler();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    RotorType(String wiringString, char[] notch) {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = wiringString;
        this.notch = notch;
        this.staticc = false;
        try {
            freshScrambler();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    RotorType(String alphabetString, String wiringString, char[] notch) {
        this.alphabetString = alphabetString;
        this.wiringString = wiringString;
        this.notch = notch;
        this.staticc = false;
        try {
            freshScrambler();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    @Override
    public Rotor freshScrambler() throws ScramblerSettingException {
        freshRotor();
        return rotor;
    }

    private void freshRotor() throws ScramblerSettingException {
        this.rotor = new Rotor(alphabetString, wiringString, notch, staticc, this);
    }

    @Override
    public String getName() {
        return this.name();
    }
}
