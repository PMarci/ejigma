package camel.enigma.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collector;

public class Util {

    private Util() {
        // static helper class
    }

    public static <K, V> Collector<Map.Entry<K, V>, Map<K, V>, Map<K, V>> collectToMap() {
        return Collector.of(
                HashMap::new,
                (kvMap, kvEntry) -> kvMap.put(kvEntry.getKey(), kvEntry.getValue()),
                (map, map2) -> {
                    map.putAll(map2);
                    return map;
                });
    }

    public static boolean containsChar(char[] array, char inputChar) {
        for (char c : array) {
            if (c == inputChar) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsChar(String string, char inputChar) {
        return string.indexOf(inputChar) != -1;
    }

    // TODO result has to give 2-cycles
    public static String fisherYatesShuffle(String alphabet) {
        Random random = ThreadLocalRandom.current();
        int[] alphaArray = alphabet.codePoints().toArray();
        for (int i = alphaArray.length - 1; i > 0; i--) {
            int rnd = random.nextInt(i + 1);
            int a = alphaArray[rnd];
            alphaArray[rnd] = alphaArray[i];
            alphaArray[i] = a;

        }
        return new String(alphaArray, 0, alphaArray.length);
    }
}
