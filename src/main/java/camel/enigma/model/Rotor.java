package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;

class Rotor extends Scrambler {

    private char ringSetting;
    private RotorType rotorType;

    Rotor(String wirings) throws ScramblerSettingException {
        validateWiringString(wirings);
        this.wirings = stringToWirings(wirings);
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        input.putResult(get(input.getResult()), rotorType.name());
        return input;
    }

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
}
