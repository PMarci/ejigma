
package camel.enigma.model.historic;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.Rotor;
import camel.enigma.model.Scrambler;
import camel.enigma.model.type.RotorType;

public enum HistoricRotorType implements RotorType {

    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ", new char[]{'Q'}),
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE", new char[]{'E'}),
    III("BDFHJLCPRTXVZNYEIWGAKMUSQO", new char[]{'V'}),
    IV("ESOVPZJAYQUIRHXLNFTGKDCMWB", new char[]{'J'}),
    V("VZBRGITYUPSDNHLXAWMJQOFECK", new char[]{'Z'}),
    VI("JPGVOUMFYQBENHZRDKASXLICTW", new char[]{'M', 'Z'}),
    VII("NZJHGRCXMYSWBOUFAIVLPEKQDT", new char[]{'M', 'Z'}),
    VIII("FKQHTLXOCBJSPDZRAMEWNIUYGV", new char[]{'M', 'Z'});

    private final String alphabetString;
    private final char[] alphabet;
    private final String wiringString;
    private final char[] notch;
    private final boolean staticc;

    HistoricRotorType(String wiringString, char[] notch) {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.alphabet = Scrambler.DEFAULT_ALPHABET_STRING.toCharArray();
        this.wiringString = wiringString;
        this.notch = notch;
        this.staticc = false;
    }

    @Override
    public Rotor freshScrambler() {
        Rotor rotor = null;
        try {
            rotor = new Rotor(alphabetString, wiringString, notch, staticc, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
        return rotor;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
