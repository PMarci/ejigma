package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

// TODO what to keep
public enum ReflectorType implements ScramblerType<ReflectorType, Reflector> {

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
    private final String wiringString;
    private final String alphabetString;

    ReflectorType() {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = Scrambler.DEFAULT_ALPHABET_STRING;
        try {
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    ReflectorType(String wiringString) {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = wiringString;
        try {
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    ReflectorType(String alphabetString, String wiringString) {
        this.alphabetString = alphabetString;
        this.wiringString = wiringString;
        try {
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    @Override
    public ReflectorType fresh() throws ScramblerSettingException {
        this.reflector = new Reflector(alphabetString, wiringString, this);
        return this;
    }

    @Override
    public Reflector freshScrambler() throws ScramblerSettingException {
        this.reflector = new Reflector(alphabetString, wiringString, this);
        return reflector;
    }

    public Reflector getReflector() {
        return reflector;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
