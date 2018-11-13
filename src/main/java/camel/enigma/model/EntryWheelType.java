package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public enum EntryWheelType implements ScramblerType<EntryWheel> {
    ENIGMA_I(Scrambler.DEFAULT_ALPHABET_STRING);

    private EntryWheel entryWheel;
    private String wiringString;

    EntryWheelType(String wiringString) {
        try {
            this.wiringString = wiringString;
            freshScrambler();
        } catch (ScramblerSettingException ignored) {
            // needed to handle constructor exception
        }
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
}
