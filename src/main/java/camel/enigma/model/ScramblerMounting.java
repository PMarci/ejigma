package camel.enigma.model;

public class ScramblerMounting {

    private Scrambler scrambler;
    private boolean reverseWired;

    public ScramblerMounting(Scrambler scrambler, boolean reverseWired) {
        this.scrambler = scrambler;
        this.reverseWired = reverseWired;
    }

    public ScramblerMounting(Scrambler scrambler) {
        this.scrambler = scrambler;
    }

    public Scrambler getScrambler() {
        return scrambler;
    }

    public void setScrambler(Scrambler scrambler) {
        this.scrambler = scrambler;
    }

    public boolean isReverseWired() {
        return reverseWired;
    }

    public void setReverseWired(boolean reverseWired) {
        this.reverseWired = reverseWired;
    }
}
