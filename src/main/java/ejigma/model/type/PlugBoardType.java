package ejigma.model.type;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.PlugBoard;

public interface PlugBoardType extends ScramblerType<PlugBoard> {

    String getSourceString();

    String getWiringString();

    String getAlphabetString();

    String getInitString();

    PlugBoard unsafeScrambler() throws ScramblerSettingException;
}
