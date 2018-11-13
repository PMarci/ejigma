package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;

class Reflector extends ScramblerWheel {

    // TODO remove tests and const if useless
    Reflector(String wiringString) throws ScramblerSettingException {
        this(DEFAULT_ALPHABET_STRING, wiringString, true, RotorType.NOOP);
    }

    Reflector(String alphabetString, String wiringString, ScramblerType scramblerType) throws ScramblerSettingException {
        this(alphabetString, wiringString, true, scramblerType);
    }

    private Reflector(String alphabetString, String wiringString, boolean staticc, ScramblerType scramblerType) throws ScramblerSettingException {
        super(alphabetString, wiringString, staticc, scramblerType);
    }

    @Override
    protected ScrambleResult scrambleInput(ScrambleResult input, int[] links) {
        int inputPos = input.getResult();
        char wiringInput = alphabet[inputPos];
        int link = links[inputPos];
        char wiringOutput = alphabet[link];
        return input.putResult(link, wiringInput, wiringOutput, wiringOutput, type.getName());
    }


    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        return scramble(input);
    }

    @Override
    public boolean isNotchEngaged() {
        return false;
    }
}
