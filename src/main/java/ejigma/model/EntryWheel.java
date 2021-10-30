package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.EntryWheelType;
import ejigma.util.ScrambleResult;

public class EntryWheel extends ScramblerWheel<EntryWheel, EntryWheelType> {

    public EntryWheel(String wiringString, EntryWheelType entryWheelType) throws ScramblerSettingException {
        this(DEFAULT_ALPHABET_STRING, wiringString, entryWheelType);
    }

    public EntryWheel(
            String alphabetString,
            String wiringString,
            EntryWheelType entryWheelType) throws ScramblerSettingException {
        super(alphabetString, wiringString, true, entryWheelType);
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        input.putCharInputToIntResult();
        return super.scramble(input);
    }

    @Override
    char scramble(char input) {
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
    protected char scrambleInput(char input, int[] links) {
        int inputPos = this.alphabetString.indexOf(input);
        int link = links[inputPos];
        return alphabet[link];
    }

    @Override
    protected boolean isNotchEngaged() {
        return false;
    }

}
