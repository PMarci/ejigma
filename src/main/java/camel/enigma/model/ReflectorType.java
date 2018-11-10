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
    NOOP(),
    ERROR1("ABC"),
    ERROR2("ABC", "ABCD"),
    NOERROR2("ABC", "ABC");

    private Reflector reflector;

    ReflectorType() {
        try {
            this.reflector = new Reflector(this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    ReflectorType(String wiringString) {
        try {
            this.reflector = new Reflector(wiringString, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    ReflectorType(String alphabetString, String wiringString) {
        try {
            this.reflector = new Reflector(alphabetString, wiringString, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }


    public Wiring[] getWirings() {
        return reflector.getWirings();
    }

    public Reflector getReflector() {
        return reflector;
    }
}
