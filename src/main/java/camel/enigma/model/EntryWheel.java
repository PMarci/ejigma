package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;

public class EntryWheel extends ScramblerWheel {

    EntryWheel(String wiringString, ScramblerType scramblerType) throws ScramblerSettingException {
        this(DEFAULT_ALPHABET_STRING, wiringString, scramblerType);
    }

    private EntryWheel(String alphabetString, String wiringString, ScramblerType scramblerType) throws ScramblerSettingException {
        super(alphabetString, wiringString, true, scramblerType);
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        initWheelPosition(input);
        return super.scramble(input);
    }

    private void initWheelPosition(ScrambleResult input) {
        int wheelPos = alphabetString.indexOf(input.getResultAsChar());
        input.setResult(wheelPos);
    }

    @Override
    protected boolean isNotchEngaged() {
        return false;
    }

}
