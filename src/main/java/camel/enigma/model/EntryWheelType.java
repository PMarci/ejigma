package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

// TODO what to keep
public enum EntryWheelType implements ScramblerType<EntryWheelType, EntryWheel> {
    ENIGMA_I(Scrambler.DEFAULT_ALPHABET_STRING);

    private EntryWheel entryWheel;
    private String wiringString;

    EntryWheelType(String wiringString) {
        try {
            this.wiringString = wiringString;
            fresh();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
    }

    @Override
    public EntryWheelType fresh() throws ScramblerSettingException {
        freshEntryWheel();
        return this;
    }

    @Override
    public EntryWheel freshScrambler() throws ScramblerSettingException {
        freshEntryWheel();
        return entryWheel;
    }

    private void freshEntryWheel() throws ScramblerSettingException {
        this.entryWheel = new EntryWheel(wiringString, this);
    }

    @Override
    public String getName() {
        return this.name();
    }

    public EntryWheel getEntryWheel() {
        return entryWheel;
    }
}
