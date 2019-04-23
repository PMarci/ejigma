package camel.enigma.model;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles("test")
public class RotorTest {

    @Test
    public void testCorrectConstructor() {
//        HistoricRotorType expected = HistoricRotorType.NOOP;
        // using getter for coverage
        // TODO fix
//        assertArrayEquals(expected.getRotor().getWirings(), HistoricRotorType.NOOP.getWirings());
    }

//    @Test(expected = ScramblerSettingLengthException.class)
//    public void testIncorrectLengthConstructor() throws ScramblerSettingException {
//        String incorrectLengthString = "AABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        new Rotor(incorrectLengthString);
//    }

//    @Test(expected = ScramblerSettingWiringException.class)
//    public void testIncorrectWiringConstructor() throws ScramblerSettingException {
//        String incorrectWiringsString = "AACDEFGHIJKLMNOPQRSTUVWXYZ";
//        new Rotor(incorrectWiringsString);
//    }
}