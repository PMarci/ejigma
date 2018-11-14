package camel.enigma.model;

import camel.enigma.util.ScrambleResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({"routeless", "test"})
public class ScramblerTest {

    private Rotor errorRotor;

    @Before
    public void setUp() throws Exception {
        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
        errorRotor = new Rotor(incorrectWiringsString);
    }
    // TODO fix

    @Test
    public void testPutAllSizeRestriction() {
        List<String> lines = ScrambleResult.HistoryEntry.loadLetter(4);
        String linesString;
        List<String> lines2 = ScrambleResult.HistoryEntry.loadLetter(26);
        int linesSize = lines.size();
        int linesSize2 = lines2.size();
        for (int i = 0; i < linesSize || i < linesSize2; i++) {
            StringBuilder line = new StringBuilder(linesString = (i < linesSize) ? lines.get(i) : "").append(ScrambleResult.HistoryEntry.pad(linesString, 22)).append((i < linesSize2) ? lines2.get(i) : "");
            System.out.println(line);
        }
    }
}