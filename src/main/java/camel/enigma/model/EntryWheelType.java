package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum EntryWheelType implements ScramblerType {
    ENIGMA_I(Scrambler.DEFAULT_ALPHABET_STRING);

    private EntryWheel entryWheel;

    EntryWheelType(String wiringString) {
        try {
            this.entryWheel = new EntryWheel(wiringString, this);
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    @Override
    public String getName() {
        return this.name();
    }

    public EntryWheel getEntryWheel() {
        return entryWheel;
    }
}
