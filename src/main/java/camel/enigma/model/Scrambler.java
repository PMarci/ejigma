package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.exception.ScramblerSettingLengthException;
import camel.enigma.exception.ScramblerSettingWiringException;

import java.util.Arrays;
import java.util.stream.Collector;

abstract class Scrambler {

    static final String ALPHABET_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final char[] ALPHABET = ALPHABET_STRING.toCharArray();

    Wiring[] wirings;

    static Wiring[] stringToWirings(String wirings) {
        Wiring[] result = new Wiring[26];
        char[] wiringsChars = wirings.toCharArray();
        for (int i = 0, length = wiringsChars.length; i < length; i++) {
            result[i] = new Wiring(Scrambler.ALPHABET[i], wiringsChars[i]);
        }
        return result;
    }

    static String wiringsToString(Wiring[] wirings) {
        return Arrays.stream(wirings).sequential()
            .map(Wiring::getSource)
            .collect(Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
    }

    void validateWiringString(String string) throws ScramblerSettingException {
        if (string.length() != 26) {
            throw new ScramblerSettingLengthException("Wirings only accept 26 char strings!");
        }
        for (char c : Scrambler.ALPHABET) {
            int freq = countOccurrences(string, c);
            if (freq > 1) {
                throw new ScramblerSettingWiringException("Scrambler wirings can only map each letter once!");
            }
        }
    }

    public Wiring[] getWirings() {
        return wirings;
    }

    public void setWirings(Wiring[] wirings) {
        this.wirings = wirings;
    }

    protected int countOccurrences(String s, char inputChar) {
        int result = 0;
        for (char c : s.toCharArray()) {
            if (c == inputChar) {
                result++;
            }
        }
        return result;
    }
}
