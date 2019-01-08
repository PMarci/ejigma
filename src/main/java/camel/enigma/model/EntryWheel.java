package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.type.EntryWheelType;
import camel.enigma.util.ScrambleResult;

public class EntryWheel extends ScramblerWheel {

    public EntryWheel(String wiringString, EntryWheelType entryWheelType) throws ScramblerSettingException {
        this(DEFAULT_ALPHABET_STRING, wiringString, entryWheelType);
    }

    public EntryWheel(String alphabetString, String wiringString, EntryWheelType entryWheelType) throws ScramblerSettingException {
        super(alphabetString, wiringString, true, entryWheelType);
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        // TODO proper
        if (input.getHistory().size() < 2) {
            input.putCharInputToIntResult();
        }
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

    public static EntryWheelType auto(String alphabetString) {
        return new EntryWheelType() {
            @Override
            public String getName() {
                return "AUTO_ENTRY";
            }

            @Override
            public EntryWheel freshScrambler() {
                EntryWheel entryWheel = null;
                try {
                    entryWheel = new EntryWheel(alphabetString, alphabetString, this);
                } catch (ScramblerSettingException e) {
                    e.printStackTrace();
                }
                return entryWheel;
            }

            @Override
            public String getAlphabetString() {
                return alphabetString;
            }
        };
    }

}
