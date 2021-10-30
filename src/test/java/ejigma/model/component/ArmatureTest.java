package ejigma.model.component;

import ejigma.exception.ScramblerSettingException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArmatureTest {

    private final String thirtyAs = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private final String wikiResult30 = "BDZGOWCXLTKSBTMCDLPBMUQOFXYHCX";
    private Enigma wikiEnigma;

    @Before
    public void setUp() throws Exception {
        wikiEnigma = new Enigma();
        Armature armature = new Armature(wikiEnigma);
        wikiEnigma.setArmature(armature);
    }

    @Test
    public void testHandle() {

        String inputString = thirtyAs.substring(0, 5);
        String output = wikiEnigma.scramble(inputString);

        assertEquals(wikiResult30.substring(0, 5), output);
    }

    @Test
    public void testScramble() {

        String inputString = thirtyAs.substring(0, 5);
        String output = wikiEnigma.scramble(inputString);

        assertEquals(wikiResult30.substring(0, 5), output);
    }

    @Test
    public void testHandleABitLonger() {

        String output = wikiEnigma.scramble(thirtyAs);

        assertEquals(wikiResult30, output);
    }

    @Test
    public void testHandleTooLong() {
        String inputString = "A".repeat(16930);

        String output = wikiEnigma.scramble(inputString);

        assertEquals(wikiResult30, output.substring(16900, 16930));
    }

    @Test
    public void testReciprocity() {

        String output = wikiEnigma.scramble(wikiResult30);

        assertEquals(thirtyAs, output);
    }

    @Test
    public void testDoubleStep() throws ScramblerSettingException {
        // TODO pdf with ADO and wiki example with ADV but with the third notch aligned as well (PDV)
//        wikiEnigma.forceSetRotors(new RotorType[]{HistoricRotorType.I, HistoricRotorType.II, HistoricRotorType.III});
        wikiEnigma.setOffsets("PDV");
        String output = wikiEnigma.scramble(thirtyAs + thirtyAs + thirtyAs);
    }
}