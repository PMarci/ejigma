
package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum RotorType implements ScramblerType {

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

    RotorType() {
        try {
            this.rotor = new Rotor(this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    RotorType(String wiringString, char[] notch) {
        try {
            this.rotor = new Rotor(wiringString, notch, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    RotorType(String alphabetString, String wiringString, char[] notch) {
        try {
            this.rotor = new Rotor(alphabetString, wiringString, notch, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    public Rotor getRotor() {
        return rotor;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
