
package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum RotorType {

//     ABCDEFGHIJKLMNOPQRSTUVWXYZ
    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ"),
//      ABCDEFGHIJKLMNOPQRSTUVWXYZ
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE"),
//       ABCDEFGHIJKLMNOPQRSTUVWXYZ
    III("BDFHJLCPRTXVZNYEIWGAKMUSQO"),
    // for testing
    NOOP(Scrambler.ALPHABET_STRING),
    ERROR("ABC");

    private Rotor rotor;
    private char[] notch;

    RotorType(String wirings) {
        try {
            this.rotor = new Rotor(wirings);
            this.rotor.setRotorType(this);
            // TODO what can be done here?
        } catch (ScramblerSettingException ignored) {
        }
    }

    public Rotor getRotor() {
        return rotor;
    }

    public char[] getNotch() {
        return notch;
    }

    public void setNotch(char[] notch) {
        this.notch = notch;
    }

    public Character get(Character key) {
        return rotor.get(key);
    }

    public Wiring[] getWirings() {
        return rotor.getWirings();
    }

    public void setWirings(Wiring[] wirings) {
        rotor.setWirings(wirings);
    }
}
