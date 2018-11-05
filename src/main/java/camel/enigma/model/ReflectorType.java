package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum ReflectorType {

//     ABCDEFGHIJKLMNOPQRSTUVWXYZ
    A("EJMZALYXVBWFCRQUONTSPIKHGD"),
//     ABCDEFGHIJKLMNOPQRSTUVWXYZ
    B("YRUHQSLDPXNGOKMIEBFZCWVJAT"),
//     ABCDEFGHIJKLMNOPQRSTUVWXYZ
    C("FVPJIAOYEDRZXWGCTKUQSBNMHL"),
    // for testing
    NOOP(Scrambler.ALPHABET_STRING),
    ERROR("ABC");

    private Reflector reflector;

    ReflectorType(String wirings) {
        try {
            this.reflector = new Reflector(wirings);
            this.reflector.setReflectorType(this);
        } catch (ScramblerSettingException ignored) {
        }
    }

    public Wiring[] getWirings() {
        return reflector.getWirings();
    }

    public void setWirings(Wiring[] wirings) {
        reflector.setWirings(wirings);
    }

    public Reflector getReflector() {
        return reflector;
    }
}
