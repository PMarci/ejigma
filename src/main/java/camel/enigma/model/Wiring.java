package camel.enigma.model;

public class Wiring {

    private char source;
    private char target;

    public Wiring(char source, char target) {
        this.source = source;
        this.target = target;
    }

    public char getSource() {
        return source;
    }

    public void setSource(char source) {
        this.source = source;
    }

    public char getTarget() {
        return target;
    }

    public void setTarget(char target) {
        this.target = target;
    }
}
