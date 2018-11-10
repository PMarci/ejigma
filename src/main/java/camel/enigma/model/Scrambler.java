package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.exception.ScramblerSettingLengthException;
import camel.enigma.exception.ScramblerSettingWiringException;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Scrambler implements Map<Character, Character> {

    static final String DEFAULT_ALPHABET_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final char[] DEFAULT_ALPHABET = DEFAULT_ALPHABET_STRING.toCharArray();
    private final String alphabetString;
    final char[] alphabet;

    private String wiringString;
    Wiring[] wirings;

    public Scrambler() throws ScramblerSettingException {
        this(DEFAULT_ALPHABET_STRING, DEFAULT_ALPHABET_STRING);
    }

    public Scrambler(String wiringString) throws ScramblerSettingException {
        this.alphabetString = DEFAULT_ALPHABET_STRING;
        this.alphabet = DEFAULT_ALPHABET;
        validateWiringString(wiringString);
        this.wiringString = wiringString;
        this.wirings = stringToDefaultWirings(wiringString);
    }

    public Scrambler(String alphabetString, String wiringString) throws ScramblerSettingException {
        this.alphabetString = alphabetString;
        this.alphabet = this.alphabetString.toCharArray();
        validateWiringString(wiringString);
        this.wiringString = wiringString;
        this.wirings = stringToWirings(wiringString);
    }

    abstract ScrambleResult scramble(ScrambleResult input);

    abstract ScrambleResult reverseScramble(ScrambleResult input);

    static Wiring[] stringToDefaultWirings(String wirings) {
        Wiring[] result = new Wiring[DEFAULT_ALPHABET.length];
        char[] wiringsChars = wirings.toCharArray();
        for (int i = 0, length = wiringsChars.length; i < length; i++) {
            result[i] = new Wiring(Scrambler.DEFAULT_ALPHABET[i], wiringsChars[i]);
        }
        return result;
    }

    private Wiring[] stringToWirings(String wirings) {
        Wiring[] result = new Wiring[alphabet.length];
        char[] wiringsChars = wirings.toCharArray();
        for (int i = 0, length = wiringsChars.length; i < length; i++) {
            result[i] = new Wiring(alphabet[i], wiringsChars[i]);
        }
        return result;
    }

    private void validateWiringString(String string) throws ScramblerSettingException {
        if (string.length() != alphabet.length) {
            throw new ScramblerSettingLengthException(String.format("Wirings only accept %d char strings!", alphabet.length));
        }
        for (char c : alphabet) {
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

    private int countOccurrences(String s, char inputChar) {
        int result = 0;
        for (char c : s.toCharArray()) {
            if (c == inputChar) {
                result++;
            }
        }
        return result;
    }

    @Override
    public int size() {
        return wirings.length;
    }

    @Override
    public boolean isEmpty() {
        return wirings.length == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof Character) {
            Character character = (Character) value;
            for (Wiring wiring : wirings) {
                if (wiring.getTarget() == character) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Character get(Object key) {
        Character result = null;
        if (key instanceof Character) {
            Character character = (Character) key;
            Optional<Wiring> resultWiringOpt = Arrays.stream(wirings).sequential()
                    .filter(wiring -> wiring != null && wiring.getSource() == character)
                    .findAny();
            if (resultWiringOpt.isPresent()) {
                result = resultWiringOpt.get().getTarget();
            }
        }
        return result;
    }

    @Override
    public Character put(Character key, Character value) {
        Character previous = null;
        for (Wiring wiring : wirings) {
            if (wiring.getSource() == key) {
                previous = wiring.getTarget();
                wiring.setTarget(value);
            }
        }
        return previous;
    }

    @Override
    public Character remove(Object key) {
        Character result = null;
        if (key instanceof Character) {
            Character character = (Character) key;
            for (int i = 0, wiringsLength = wirings.length; i < wiringsLength; i++) {
                Wiring wiring = wirings[i];
                if (wiring.getSource() == character) {
                    wirings[i] = null;
                    result = wiring.getTarget();
                }
            }
        }
        return result;
    }

    @Override
    public void putAll(Map<? extends Character, ? extends Character> m) {
        ArrayDeque<Integer> nullIndices = new ArrayDeque<>();
        Map<Character, Integer> sourceIndexMap = IntStream.range(0, wirings.length).sequential()
                .mapToObj(value -> {
                    AbstractMap.SimpleEntry<Character, Integer> result = null;
                    Wiring wiring = wirings[value];
                    if (wiring != null) {
                        result = new AbstractMap.SimpleEntry<>(wiring.getSource(), value);
                    } else {
                        nullIndices.addLast(value);
                    }
                    return result;})
                .filter(Objects::nonNull)
                .collect(Util.collectToMap());
        for (Entry<? extends Character, ? extends Character> entry : m.entrySet()) {
            Character source = entry.getKey();
            Character target = entry.getValue();
            Integer index = sourceIndexMap.get(source);
            Wiring newWiring = new Wiring(source, target);
            Integer nullIndex;
            if (index != null) {
                wirings[index] = newWiring;
            } else if ((nullIndex = nullIndices.pollFirst()) != null) {
                wirings[nullIndex] = newWiring;
            } else {
                // TODO simplify check free space further up
                throw new IllegalArgumentException("can't add any more wirings!");
            }
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < wirings.length; i++) {
            wirings[i] = null;
        }
    }

    @Override
    public Set<Character> keySet() {
        return Arrays.stream(wirings).sequential()
                .filter(Objects::nonNull)
                .map(Wiring::getSource)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Character> values() {
        return Arrays.stream(wirings).sequential()
                .filter(Objects::nonNull)
                .map(Wiring::getTarget)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<Character, Character>> entrySet() {
        return Arrays.stream(wirings).sequential()
                .filter(Objects::nonNull)
                .map(wiring -> new AbstractMap.SimpleEntry<>(wiring.getSource(), wiring.getTarget()))
                .collect(Collectors.toSet());
    }

    public String getWiringString() {
        return wiringString;
    }
}
