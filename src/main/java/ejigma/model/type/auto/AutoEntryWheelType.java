package ejigma.model.type.auto;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.EntryWheel;
import ejigma.model.type.EntryWheelType;
import ejigma.util.Util;

public class AutoEntryWheelType implements EntryWheelType {

    private static final String NAME = "AUTO_ENTRY";
    private final String alphabetString;

    public AutoEntryWheelType(String alphabetString) {
        this.alphabetString = alphabetString;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EntryWheel freshScrambler() {
        EntryWheel entryWheel = null;
        try {
            entryWheel = new EntryWheel(alphabetString, Util.generate2Cycles(alphabetString), this);
        } catch (ScramblerSettingException e) {
            e.printStackTrace();
        }
        return entryWheel;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }
    @Override
    public String toString() {
        return getName();
    }
}
