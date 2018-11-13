
package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

// TODO what to keep
public enum RotorType implements ScramblerType<RotorType, Rotor> {

    //     ABCDEFGHIJKLMNOPQRSTUVWXYZ
    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ", new char[]{'Q'}),
    //      ABCDEFGHIJKLMNOPQRSTUVWXYZ
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE", new char[]{'E'}),
    //       ABCDEFGHIJKLMNOPQRSTUVWXYZ
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

    RotorType() {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.notch = Rotor.DEFAULT_NOTCH;
        try {
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    RotorType(String wiringString, char[] notch) {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = wiringString;
        this.notch = notch;
        try {
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    RotorType(String alphabetString, String wiringString, char[] notch) {
        this.alphabetString = alphabetString;
        this.wiringString = wiringString;
        this.notch = notch;
        try {
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    @Override
    public RotorType fresh() throws ScramblerSettingException {
        this.rotor = new Rotor(wiringString, notch, this);
        return this;
    }

    @Override
    public Rotor freshScrambler() throws ScramblerSettingException {
        this.rotor = new Rotor(wiringString, notch, this);
        return rotor;
    }

    public Rotor getRotor() {
        return rotor;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
