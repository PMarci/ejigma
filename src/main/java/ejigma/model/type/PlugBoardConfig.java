package ejigma.model.type;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.PlugBoard;

import java.util.Locale;

public class PlugBoardConfig implements ScramblerType<PlugBoard, PlugBoardConfig> {

    // not final because of JAXB
    protected String sourceString;
    protected String initString;
    protected String wiringString;
    protected String alphabetString;

    protected PlugBoardConfig() {
        // jaxb
    }

    public PlugBoardConfig(String sourceString, String wiringString, String alphabetString) {
        this.sourceString = sourceString;
        this.wiringString = wiringString;
        this.initString = getInitString();
        this.alphabetString = alphabetString;
    }

    public PlugBoardConfig(String alphabetString, String initString) throws ScramblerSettingException {
        String[] splitInitString = PlugBoard.splitInitString(alphabetString, initString.toUpperCase(Locale.ROOT));
        this.initString = initString;
        this.sourceString = splitInitString[0];
        this.wiringString = splitInitString[1];
        this.alphabetString = alphabetString;
    }

    public PlugBoard freshScrambler() {
        PlugBoard plugBoard = null;
        try {
            plugBoard = unsafeScrambler();
        } catch (ScramblerSettingException e) {
            e.printStackTrace();
        }
        return plugBoard;
    }

    public PlugBoard unsafeScrambler() throws ScramblerSettingException {
        return new PlugBoard(getAlphabetString(), getSourceString(), getWiringString());
    }

    @Override
    public String getName() {
        return "PLUGBOARD";
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getSourceString() {
        return sourceString;
    }

    public String getWiringString() {
        return wiringString;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }

    public String getInitString() {
        return sourceString + '\u0000' + wiringString;
    }
}
