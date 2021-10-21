package camel.enigma.model;

import camel.enigma.util.ScrambleResult;

public class ScramblerMounting {

    private final Scrambler scrambler;
    private final boolean reverseWired;

    ScramblerMounting(Scrambler scrambler, boolean reverseWired) {
        this.scrambler = scrambler;
        this.reverseWired = reverseWired;
    }

    ScramblerMounting(Scrambler scrambler) {
        this.scrambler = scrambler;
        this.reverseWired = false;
    }

    ScrambleResult scramble(ScrambleResult input) {
        return !reverseWired ? scrambler.scramble(input) : scrambler.reverseScramble(input);
    }

    public Scrambler getScrambler() {
        return scrambler;
    }

    public boolean isReverseWired() {
        return reverseWired;
    }
}
