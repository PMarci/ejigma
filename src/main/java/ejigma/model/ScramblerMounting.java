package ejigma.model;

import ejigma.model.type.ScramblerType;
import ejigma.util.ScrambleResult;

public class ScramblerMounting<S extends Scrambler<S, T>, T extends ScramblerType<S, T>> {

    private final Scrambler<S, T> scrambler;
    private final boolean reverseWired;

    ScramblerMounting(Scrambler<S, T> scrambler, boolean reverseWired) {
        this.scrambler = scrambler;
        this.reverseWired = reverseWired;
    }

    ScramblerMounting(Scrambler<S, T> scrambler) {
        this.scrambler = scrambler;
        this.reverseWired = false;
    }

    ScrambleResult scramble(ScrambleResult input) {
        return !reverseWired ? scrambler.scramble(input) : scrambler.reverseScramble(input);
    }

    char scramble(char input) {
        return !reverseWired ? scrambler.scramble(input) : scrambler.reverseScramble(input);
    }

    public Scrambler<S, T> getScrambler() {
        return scrambler;
    }

    public boolean isReverseWired() {
        return reverseWired;
    }
}
