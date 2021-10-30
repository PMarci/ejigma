package ejigma.model.type.historic;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.component.Reflector;
import ejigma.model.component.Scrambler;
import ejigma.model.type.ReflectorType;

public enum HistoricReflectorType implements ReflectorType {

    A("EJMZALYXVBWFCRQUONTSPIKHGD"),
    B("YRUHQSLDPXNGOKMIEBFZCWVJAT"),
    C("FVPJIAOYEDRZXWGCTKUQSBNMHL");

    private final String wiringString;
    private final String alphabetString;

    HistoricReflectorType(String wiringString) {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.wiringString = wiringString;
    }

    @Override
    public Reflector freshScrambler() {
        Reflector reflector = null;
        try {
            reflector = new Reflector(alphabetString, wiringString, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
        return reflector;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String toString() {
        return getName();
    }
}
