
package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum RotorType {

    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ"),
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE"),
    III("BDFHJLCPRTXVZNYEIWGAKMUSQO"),
    // for testing
    NOOP(Scrambler.ALPHABET_STRING);

    static class Rotor extends Scrambler {

        public Rotor(String wirings) throws ScramblerSettingException {
            validateWiringString(wirings);
            this.wirings = stringToWirings(wirings);
        }
    }

    private Rotor rotor;
    private char[] notch;

    RotorType(String wirings) {
        try {
            this.rotor = new Rotor(wirings);
            // TODO what can be done here?
        } catch (ScramblerSettingException ignored) {
        }
    }

    public Wiring[] getWirings() {
        return rotor.getWirings();
    }

    public void setWirings(Wiring[] wirings) {
        rotor.setWirings(wirings);
    }
}
