package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.util.ScrambleResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlugBoardTest {

    @Test
    public void testo() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BDK", "QBZ");
        assertEquals("AQCBEFGHIJZLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test
    public void testo2() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BZK", "QBC");
        assertEquals("AQCDEFGHIJCLMNOPQRSTUVWXYB", singleScramble(plugBoard));
    }

    @Test
    public void testo3() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKZ", "APN");
        assertEquals("AACDEFGHIJPLMNOPQRSTUVWXYN", singleScramble(plugBoard));
    }

    @Test
    public void testo4() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKZ", "APZ");
        assertEquals("AACDEFGHIJPLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test
    public void testo5() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKA", "APA");
        assertEquals("AACDEFGHIJPLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test
    public void testo6() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard();
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    private static String singleScramble(Scrambler scrambler) {
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