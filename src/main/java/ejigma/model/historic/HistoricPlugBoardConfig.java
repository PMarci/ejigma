package ejigma.model.historic;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.PlugBoard;
import ejigma.model.type.PlugBoardConfig;

import static ejigma.model.Scrambler.DEFAULT_ALPHABET_STRING;

@SuppressWarnings("unused")
public enum HistoricPlugBoardConfig implements PlugBoardConfig {
    NOPLUGS();

    private final String alphabetString;
    private final char[] alphabet;
    private final String initString;
    private final String sourceString;
    private final String wiringString;

    HistoricPlugBoardConfig() {
        this(DEFAULT_ALPHABET_STRING, DEFAULT_ALPHABET_STRING.toCharArray(), "");
    }

    HistoricPlugBoardConfig(String alphabetString,
                            char[] alphabet,
                            String initString) {

        this.alphabetString = alphabetString;
        this.alphabet = alphabet;
        this.initString = initString;
        String[] splitInit = new String[0];
        try {
            splitInit = PlugBoard.splitInitString(alphabetString, initString);
        } catch (ScramblerSettingException e) {
            // ignored, this is an enum
        }
        this.sourceString = splitInit[0];
        this.wiringString = splitInit[1];
    }


    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getSourceString() {
        return sourceString;
    }

    @Override
    public String getWiringString() {
        return wiringString;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }

    @Override
    public String getInitString() {
        return initString;
    }

    @Override
    public String toString() {
        return getName();
    }
}
