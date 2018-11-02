package camel.enigma.model;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.AbstractMap;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

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
}