package camel.enigma.model.type;

import camel.enigma.model.Reflector;

public class CustomReflectorType implements ReflectorType {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Reflector freshScrambler() {
        return null;
    }

    @Override
    public String getAlphabetString() {
        return null;
    }
}
