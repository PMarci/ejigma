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
}
