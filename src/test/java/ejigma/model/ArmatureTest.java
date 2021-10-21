package ejigma.model;

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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16280; i++) {
            sb.append('A');
        }
        String inputString = sb.toString();

        String output = wikiEnigma.scramble(inputString);

        assertEquals(wikiResult30, output.substring(16250, 16280));
    }

    @Test
    public void testReciprocity() {

        String output =  wikiEnigma.scramble(wikiResult30);

        assertEquals(thirtyAs, output);
    }
}