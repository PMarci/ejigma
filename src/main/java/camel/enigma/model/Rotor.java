package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;

import java.util.Arrays;
import java.util.Optional;

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
        // sfatic
        char inputPos = input.getResult();
        char key = Util.wrapOverflow(inputPos + Util.offsetToIndex(offset));
        char value = get(key);
        char outputPos = Util.wrapOverflow(value - Util.offsetToIndex(offset));
        input.putResult(outputPos, rotorType.name(), key, value, offset);
        return input;
    }
// TODO uh-oh
// TODO BiMap?
    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        // TODO figure out offset here properly
        char inputPos = input.getResult();
        char key = Util.wrapOverflow(inputPos + Util.offsetToIndex(offset));
        // TODO real impl
        Optional<Character> valueOpt = Arrays.stream(wirings).filter(wiring -> key == wiring.getTarget()).map(Wiring::getSource).findAny();
        char value = 0;
        if (valueOpt.isPresent()) {
            value = valueOpt.get();
        } else {
            System.out.println("null");
        }
        char outputPos = Util.wrapOverflow(value - Util.offsetToIndex(offset));
        input.putResult(outputPos, rotorType.name(), key, value, offset);
        return input;
    }

    // TODO non-historical considerations?
    // TODO I could just as well offset the source like in below implementation,
    // TODO and maybe even consider the static entrypoint as source
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
