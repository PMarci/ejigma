package ejigma.model.type.auto;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.Reflector;
import ejigma.model.type.ReflectorType;
import ejigma.util.Util;

public class AutoReflectorType implements ReflectorType {

    private static final String NAME = "AUTO_REFLECTOR";
    private final String alphabetString;

    public AutoReflectorType(String alphabetString) {
        this.alphabetString = alphabetString;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Reflector freshScrambler() {
        Reflector reflector = null;
        try {
            reflector = new Reflector(alphabetString, Util.generate2Cycles(alphabetString), this);
        } catch (ScramblerSettingException e) {
            e.printStackTrace();
        }
        return reflector;
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
