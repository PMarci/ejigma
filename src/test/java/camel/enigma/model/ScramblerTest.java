package camel.enigma.model;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

// TODO figure out if needed
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({"routeless", "test"})
public class ScramblerTest {

    @Test
    public void testWiringsToString() {
        String reverseAlphabet = new StringBuilder(Scrambler.ALPHABET_STRING).reverse().toString();
        Wiring[] reverseWiring = IntStream.range(0, Scrambler.ALPHABET.length).sequential()
            .mapToObj(i -> new AbstractMap.SimpleEntry<>(Scrambler.ALPHABET[Scrambler.ALPHABET.length - i - 1], Scrambler.ALPHABET[i]))
            .map(entry -> new Wiring(entry.getKey(), entry.getValue()))
            .toArray(Wiring[]::new);

        assertEquals(reverseAlphabet, Scrambler.wiringsToString(reverseWiring));
    }

    // TODO maybe split up
    @Test
    public void testImplementedMethods() throws Exception {
        RotorType rotorTypeI = RotorType.I;
        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
        Rotor errorRotor = new Rotor(incorrectWiringsString);

        assertEquals(26, errorRotor.size());
        assertFalse(errorRotor.containsKey('@'));
        assertTrue(errorRotor.containsValue('@'));
        assertFalse(errorRotor.containsValue('B'));
        assertFalse(errorRotor.isEmpty());
        assertEquals((Character) 'E', rotorTypeI.get('A'));

        errorRotor.put('B', 'B');
        assertEquals((Character) 'B', errorRotor.get('B'));

        errorRotor.remove('B');
        assertNull(errorRotor.get('B'));

        errorRotor.clear();
        for (Wiring wiring : errorRotor.getWirings()) {
            assertNull(wiring);
        }
    }

    @Test
    public void testPutAll() throws Exception {
        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
        Rotor errorRotor = new Rotor(incorrectWiringsString);
        Map<Character, Character> wiringMap = new HashMap<>();
        wiringMap.put('B', '@');
        wiringMap.put('C', '&');
        wiringMap.put('D', '$');

        errorRotor.putAll(wiringMap);
        assertEquals((Character) '@', errorRotor.get('B'));
        assertEquals((Character) '&', errorRotor.get('C'));
        assertEquals((Character) '$', errorRotor.get('D'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutAllSizeRestriction() throws Exception {

        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
        Rotor errorRotor = new Rotor(incorrectWiringsString);
        Map<Character, Character> wiringMap = new HashMap<>();
        wiringMap.put('B', '@');
        wiringMap.put('C', '&');
        wiringMap.put('D', '$');
        wiringMap.put('^', '$');

        errorRotor.putAll(wiringMap);
    }
}