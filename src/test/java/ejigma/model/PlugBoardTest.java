package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.ScramblerType;
import ejigma.util.ScrambleResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlugBoardTest {

    @Test(expected = ScramblerSettingException.class)
    public void testo() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BDK", "QBZ");
        assertEquals("AQCBEFGHIJZLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test(expected = ScramblerSettingException.class)
    public void testo2() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BZK", "QBC");
        assertEquals("AQCDEFGHIJCLMNOPQRSTUVWXYB", singleScramble(plugBoard));
    }

    @Test
    public void testo3() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKZ", "APN");
        assertEquals("BACDEFGHIJPLMZOKQRSTUVWXYN", singleScramble(plugBoard));
    }

    @Test
    public void testo4() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKZ", "APZ");
        assertEquals("BACDEFGHIJPLMNOKQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test
    public void testo5() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKA", "APA");
        assertEquals("BACDEFGHIJPLMNOKQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test
    public void testo6() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard();
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    private static <S extends Scrambler<S, T>, T extends ScramblerType<S, T>> String singleScramble(Scrambler<S, T> scrambler) {
        return Scrambler.DEFAULT_ALPHABET_STRING.codePoints().sequential()
            .mapToObj(i -> new ScrambleResult(Scrambler.DEFAULT_ALPHABET_STRING, ((char) i)))
            .mapToInt(scrambleResult -> scrambler.scramble(scrambleResult).getResultAsChar())
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append)
            .toString();
    }
}