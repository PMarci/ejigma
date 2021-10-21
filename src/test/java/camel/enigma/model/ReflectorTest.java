package camel.enigma.model;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({"routeless", "test"})
public class ReflectorTest {

    @Test
    public void testCorrectConstructor() {
//        ReflectorType expected = HistoricReflectorType.NOOP;
        // using getter for coverage
        // TODO fix
//        assertArrayEquals(expected.getReflector().getWirings(), HistoricReflectorType.NOOP.getWirings());
    }

//    @Test(expected = ScramblerSettingLengthException.class)
//    public void testIncorrectLengthConstructor() throws ScramblerSettingException {
//        String incorrectLengthString = "AABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        new Reflector(incorrectLengthString);
//    }

//    @Test(expected = ScramblerSettingWiringException.class)
//    public void testIncorrectWiringConstructor() throws ScramblerSettingException {
//        String incorrectWiringsString = "AACDEFGHIJKLMNOPQRSTUVWXYZ";
//        new Reflector(incorrectWiringsString);
//    }
}