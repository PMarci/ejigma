package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertEquals;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({ "routeless", "test" })
public class PlugBoardTest {

    @Test
    public void testo() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BDK", "QBZ", RotorType.NOOP);
        assertEquals("AQCBEFGHIJZLMNOPQRSTUVWXYZ", singleScramble(plugBoard));
    }

    @Test
    public void testo2() throws ScramblerSettingException {
        PlugBoard plugBoard = new PlugBoard(Scrambler.DEFAULT_ALPHABET_STRING, "BZK", "QBC", RotorType.NOOP);
        assertEquals("AQCDEFGHIJCLMNOPQRSTUVWXYB", singleScramble(plugBoard));
    }

    private static String singleScramble(Scrambler scrambler) {
        return Scrambler.DEFAULT_ALPHABET_STRING.codePoints().sequential()
            .mapToObj(i -> new ScrambleResult(((char) i)))
            .mapToInt(scrambleResult -> scrambler.scramble(scrambleResult).getResultAsChar())
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append)
            .toString();
    }
}