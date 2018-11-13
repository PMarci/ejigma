package camel.enigma.model;

import camel.enigma.util.ScrambleResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertEquals;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({ "routeless", "test" })
public class ArmatureTest {

    private Armature wikiArmature;

    @Before
    public void setUp() throws Exception {
        wikiArmature = new Armature();
    }

    @Test
    public void testHandle() {

        String inputString = "AAAAA";
        String output = encryptString(inputString, wikiArmature);

        assertEquals("BDZGO", output);
    }

    @Test
    public void testHandleABitLonger() {

        String inputString = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String output = encryptString(inputString, wikiArmature);

        assertEquals("BDZGOWCXLTKSBTMCDLPBMUQOFXYHCX", output);
    }

    @Test
    public void testHandleTooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16280; i++) {
            sb.append('A');
        }
        String inputString = sb.toString();

        String output = encryptString(inputString, wikiArmature);

        String expected = "BDZGOWCXLTKSBTMCDLPBMUQOFXYHCX";
        assertEquals(expected, output.substring(16250, 16280));
    }

    @Test
    public void testReciprocity() {
        StringBuilder sb = new StringBuilder();

        String expxected = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String encrypted = "BDZGOWCXLTKSBTMCDLPBMUQOFXYHCX";
        String output = encryptString(encrypted, wikiArmature);
        assertEquals(expxected, output);
    }

    private static String encryptString(String inputString, Armature armature) {
        return inputString.codePoints().sequential()
            .mapToObj(i -> new ScrambleResult(((char) i)))
            .mapToInt(scrambleResult -> armature.handle(scrambleResult).getResultAsChar())
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append)
            .toString();
    }
}