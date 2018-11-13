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
        input.putCharInputToIntResult(alphabetString);
        return super.scramble(input);
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
    protected boolean isNotchEngaged() {
        return false;
    }

}
