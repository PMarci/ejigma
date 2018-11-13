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

    private final String thirtyAs = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private final String wikiResult30 = "BDZGOWCXLTKSBTMCDLPBMUQOFXYHCX";
    private Armature wikiArmature;

    @Before
    public void setUp() throws Exception {
        wikiArmature = new Armature();
    }

    @Test
    public void testHandle() {

        String inputString = thirtyAs.substring(0, 5);
        String output = encryptString(inputString, wikiArmature);

        assertEquals(wikiResult30.substring(0, 5), output);
    }

    @Test
    public void testHandleABitLonger() {

        String output = encryptString(thirtyAs, wikiArmature);

        assertEquals(wikiResult30, output);
    }

    @Test
    public void testHandleTooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16280; i++) {
            sb.append('A');
        }
        String inputString = sb.toString();

        String output = encryptString(inputString, wikiArmature);

        assertEquals(wikiResult30, output.substring(16250, 16280));
    }

    @Test
    public void testReciprocity() {

        String output = encryptString(wikiResult30, wikiArmature);

        assertEquals(thirtyAs, output);
    }

    private static String encryptString(String inputString, Armature armature) {
        return inputString.codePoints().sequential()
            .mapToObj(i -> new ScrambleResult(((char) i)))
            .mapToInt(scrambleResult -> armature.handle(scrambleResult, false).getResultAsChar())
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append)
            .toString();
    }
}