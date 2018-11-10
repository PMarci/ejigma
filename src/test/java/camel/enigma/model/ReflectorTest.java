package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.exception.ScramblerSettingLengthException;
import camel.enigma.exception.ScramblerSettingWiringException;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertArrayEquals;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({"routeless", "test"})
public class ReflectorTest {

    @Test
    public void testCorrectConstructor() {
        ReflectorType expected = ReflectorType.NOOP;
        // using getter for coverage
        assertArrayEquals(expected.getReflector().getWirings(), ReflectorType.NOOP.getWirings());
    }

    @Test(expected = ScramblerSettingLengthException.class)
    public void testIncorrectLengthConstructor() throws ScramblerSettingException {
        String incorrectLengthString = "AABCDEFGHIJKLMNOPQRSTUVWXYZ";
        new Reflector(incorrectLengthString);
    }

    @Test(expected = ScramblerSettingWiringException.class)
    public void testIncorrectWiringConstructor() throws ScramblerSettingException {
        String incorrectWiringsString = "AACDEFGHIJKLMNOPQRSTUVWXYZ";
        new Reflector(incorrectWiringsString);
    }
}