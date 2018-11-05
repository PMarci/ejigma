package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;

class Rotor extends Scrambler {

    private char ringSetting;
    private char offset = 'A';
    private RotorType rotorType;

    Rotor(String wirings) throws ScramblerSettingException {
        validateWiringString(wirings);
        this.wirings = stringToWirings(wirings);
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        // TODO figure out offset here properly
        char current = input.getResult();
        char key = Util.wrapOverflow((char) (current + Util.capitalCharToIndex(offset) - Util.capitalCharToIndex(input.getPreviousOffset())));
        input.putResult(get(key), rotorType.name(), offset);
        return input;
    }

    // TODO non-historical considerations?
    @Override
    void click() {
        if (offset < 'Z') {
            offset++;
        } else if (offset == 'Z') {
            offset = 'A';
        }
    }

    // TODO develop better internal rotor state
    // this is actually incrementing the ring setting
//    void click() {
//        for (int i = 0, wiringsLength = wirings.length; i < wiringsLength; i++) {
//            Wiring wiring = wirings[i];
//            Wiring nextWiring;
//            if (i == wiringsLength - 1) {
//                nextWiring = wirings[0];
//            } else {
//                nextWiring = wirings[i + 1];
//            }
//                wiring.setTarget(nextWiring.getTarget());
//        }
//    }

    public char getRingSetting() {
        return ringSetting;
    }

    public void setRingSetting(char ringSetting) {
        this.ringSetting = ringSetting;
    }

    public RotorType getRotorType() {
        return rotorType;
    }

    public void setRotorType(RotorType rotorType) {
        this.rotorType = rotorType;
    }

    public char getOffset() {
        return offset;
    }

    public void setOffset(char offset) {
        this.offset = offset;
    }
}
