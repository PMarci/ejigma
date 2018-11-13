package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum ReflectorType implements ScramblerType{

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
            this.reflector = new Reflector();
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

    public Reflector getReflector() {
        return reflector;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
