package ejigma.model.type;

import ejigma.model.Scrambler;

public interface ScramblerType<S extends Scrambler> {

    String getName();

    S freshScrambler();

    String getAlphabetString();

    String toString();
}
