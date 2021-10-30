package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.ScramblerType;
import ejigma.model.type.auto.AutoPlugBoardConfig;
import ejigma.util.ScrambleResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PlugBoardTest {

    @Test
    public void testo() {
        assertThrows(
                ScramblerSettingException.class,
                () -> new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BDK", "QBZ"));
    }

    @Test
    public void testo2() {
        assertThrows(
                ScramblerSettingException.class,
                () -> new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BZK", "QBC"));
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

    @Test
    public void testo7() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BKA", "KBA");
        assertEquals("AKCDEFGHIJBLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test(expected = Test.None.class)
    public void testAutoPlugBoard() throws ScramblerSettingException {
        for (int i = 0; i < 100; i++) {
            AutoPlugBoardConfig.create(Scrambler.DEFAULT_ALPHABET_STRING).unsafeScrambler();
        }
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