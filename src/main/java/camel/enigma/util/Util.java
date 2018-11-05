package camel.enigma.util;

import java.util.HashMap;
import java.util.Map;
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

    public static int capitalCharToIndex(char c) {
        return c - 65;
    }

    public static char indexTocapitalChar(int i) {
        return (char) (i + 65);
    }

    // TODO actual solution
    // TODO ints or chars internally?
    public static char wrapOverflow(char c) {
        char result;
        if (64 < c && c < 91) {
            result = c;
        } else if (90 < c) {
            result = (char) (c - 90 + 64);
        } else {
            result = (char) (c - 64 + 90);
        }

        return result;
    }
}
