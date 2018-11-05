package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;

class Reflector extends Scrambler {

    private ReflectorType reflectorType;

    public Reflector(String wirings) throws ScramblerSettingException {
        validateWiringString(wirings);
        this.wirings = stringToWirings(wirings);
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        // TODO better default
        char key = input.getResult();
        char value = get(key);
        input.putResult(value, reflectorType.name(),key, value, '@');
        return input;
    }

    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        return scramble(input);
    }

    @Override
    void click() {

    }

    public ReflectorType getReflectorType() {
        return reflectorType;
    }

    public void setReflectorType(ReflectorType reflectorType) {
        this.reflectorType = reflectorType;
    }
}
