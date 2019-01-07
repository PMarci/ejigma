package camel.enigma.model;

import camel.enigma.model.type.RotorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomRotorType implements RotorType {

    private String name;
    private String alphabetString = Scrambler.DEFAULT_ALPHABET_STRING.substring(0, 22);

    public CustomRotorType(@Value("${test.name}") String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Rotor freshScrambler() {
        return null;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }
}
