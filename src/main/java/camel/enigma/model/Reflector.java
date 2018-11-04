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
        return null;
    }

    public ReflectorType getReflectorType() {
        return reflectorType;
    }

    public void setReflectorType(ReflectorType reflectorType) {
        this.reflectorType = reflectorType;
    }
}
