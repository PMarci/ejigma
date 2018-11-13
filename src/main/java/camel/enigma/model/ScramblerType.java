package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public interface ScramblerType<S extends Scrambler> {

    String getName();

    S freshScrambler() throws ScramblerSettingException;
}
