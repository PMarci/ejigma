package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;

public interface ScramblerType<ST extends ScramblerType, S extends Scrambler> {

    String getName();

    ST fresh() throws ScramblerSettingException;

    S freshScrambler() throws ScramblerSettingException;
}
