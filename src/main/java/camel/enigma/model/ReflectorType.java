package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum ReflectorType {

    A("EJMZALYXVBWFCRQUONTSPIKHGD"),
    B("YRUHQSLDPXNGOKMIEBFZCWVJAT"),
    C("FVPJIAOYEDRZXWGCTKUQSBNMHL"),
    // for testing
    NOOP(Scrambler.ALPHABET_STRING);

    static class Reflector extends Scrambler {

        public Reflector(String wirings) throws ScramblerSettingException {
            validateWiringString(wirings);
            this.wirings = stringToWirings(wirings);
        }
    }

    private Reflector reflector;

    ReflectorType(String wirings) {
        try {
            this.reflector = new Reflector(wirings);
        } catch (ScramblerSettingException ignored) {
        }
    }

    public Wiring[] getWirings() {
        return reflector.getWirings();
    }

    public void setWirings(Wiring[] wirings) {
        reflector.setWirings(wirings);
    }
}
