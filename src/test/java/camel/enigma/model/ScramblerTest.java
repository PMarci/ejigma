package camel.enigma.model;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

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

    // TODO maybe split up
    @Test
    public void testImplementedMethods() {
        RotorType rotorTypeI = RotorType.I;

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

        Rotor typeI = RotorType.I.getRotor();
        Set<Character> defaultAlphabetSet = IntStream.range(0, Rotor.DEFAULT_ALPHABET.length).mapToObj(value -> Rotor.DEFAULT_ALPHABET[value]).collect(Collectors.toSet());
        List<Character> typeIExpectedValues = typeI.getWiringString().chars().mapToObj(value -> (char) value).collect(Collectors.toList());
        Collection<Character> typeIValues = typeI.values();
        Set<Map.Entry<Character, Character>> typeIExpectedEntries = IntStream.range(0, Rotor.DEFAULT_ALPHABET.length).mapToObj(value -> new AbstractMap.SimpleEntry<>(typeI.alphabet[value], typeIExpectedValues.get(value))).collect(Collectors.toSet());

        assertEquals(defaultAlphabetSet, typeI.keySet());
        assertEquals(typeIExpectedValues, typeIValues);
        assertEquals(typeIExpectedEntries, typeI.entrySet());
    }

    @Test
    public void testPutAll() {
        Map<Character, Character> wiringMap = new HashMap<>();
        wiringMap.put('B', '@');
        wiringMap.put('C', '&');
        wiringMap.put('D', '$');

        errorRotor.putAll(wiringMap);
        assertEquals((Character) '@', errorRotor.get('B'));
        assertEquals((Character) '&', errorRotor.get('C'));
        assertEquals((Character) '$', errorRotor.get('D'));
    }

    @Test
    public void testOverlyComplicatedPutAll() throws Exception {
        Rotor twoWireRotor = new Rotor("AB", "BA");
        twoWireRotor.clear();

        Map<Character, Character> wiringMap = new HashMap<>();
        char source1 = 'B';
        char source2 = 'C';
        char target1 = '@';
        char target2 = '&';
        wiringMap.put(source1, target1);
        wiringMap.put(source2, target2);

        twoWireRotor.putAll(wiringMap);

        assertEquals(((Character) target1), twoWireRotor.get(source1));
        assertEquals(((Character) target2), twoWireRotor.get(source2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutAllSizeRestriction() {
        Map<Character, Character> wiringMap = new HashMap<>();
        wiringMap.put('B', '@');
        wiringMap.put('C', '&');
        wiringMap.put('D', '$');
        wiringMap.put('^', '$');

        errorRotor.putAll(wiringMap);
    }
}