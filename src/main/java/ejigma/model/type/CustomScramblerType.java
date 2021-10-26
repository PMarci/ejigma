package ejigma.model.type;

import ejigma.model.Scrambler;

public interface CustomScramblerType<S extends Scrambler<S, T>, T extends ScramblerType<S, T>> extends ScramblerType<S, T> {
}
