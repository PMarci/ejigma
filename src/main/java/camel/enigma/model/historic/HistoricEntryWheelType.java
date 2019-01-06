package camel.enigma.model.historic;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.EntryWheel;
import camel.enigma.model.Scrambler;
import camel.enigma.model.type.EntryWheelType;

public enum HistoricEntryWheelType implements EntryWheelType {
    ENIGMA_I();

    private final String alphabetString;
    private final char[] alphabet;
    private String wiringString;


    HistoricEntryWheelType() {
        this.alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        this.alphabet = alphabetString.toCharArray();
        this.wiringString = alphabetString;
    }

    @Override
    public EntryWheel freshScrambler() {
        EntryWheel entryWheel = null;
        try {
            entryWheel = new EntryWheel(wiringString, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
        return entryWheel;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }
}
