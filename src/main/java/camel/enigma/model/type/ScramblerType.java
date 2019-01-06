package camel.enigma.model.type;

import camel.enigma.model.Scrambler;

public interface ScramblerType<S extends Scrambler> {

    String getName();

    S freshScrambler();

    String getAlphabetString();
}
