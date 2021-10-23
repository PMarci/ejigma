package ejigma.model.type;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.PlugBoard;

public interface PlugBoardConfig extends ScramblerType<PlugBoard> {

    String getSourceString();

    String getWiringString();

    String getAlphabetString();

    String getInitString();

    default PlugBoard freshScrambler() {
        PlugBoard plugBoard = null;
        try {
            plugBoard = unsafeScrambler();
        } catch (ScramblerSettingException e) {
            e.printStackTrace();
        }
        return plugBoard;
    }

    default PlugBoard unsafeScrambler() throws ScramblerSettingException {
        return new PlugBoard(getAlphabetString(), getSourceString(), getWiringString());
    }
}
