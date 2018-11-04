package camel.enigma.model;

import camel.enigma.util.ScrambleResult;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class Armature {

    private RotorType[] defaultRotorTypes = new RotorType[]{RotorType.I, RotorType.II, RotorType.III};
    private ReflectorType defaultReflectorType = ReflectorType.A;
    private Rotor[] rotors;
    private Reflector reflector;
    private List<Scrambler> scramblerWiring = initWirings(defaultRotorTypes, defaultReflectorType);

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {
        ScrambleResult current = scrambleResult;
        for (Scrambler scrambler : scramblerWiring) {
            current = scrambler.scramble(current);
        }
        return current;
    }

    private List<Scrambler> initWirings(RotorType[] rotorTypes, ReflectorType reflectorType) {
        List<Scrambler> resultList = IntStream.range(0, rotorTypes.length * 2 + 1).sequential()
                .mapToObj(value -> {
                    Scrambler result;
                    if (value < rotorTypes.length) {
                        result = rotorTypes[value].getRotor();
                    } else if (value == rotorTypes.length) {
                        result = reflectorType.getReflector();
                    } else {
                        result = rotorTypes[rotorTypes.length * 2 - value].getRotor();
                    }
                    return result;
                })
                .collect(Collectors.toList());

        return resultList;
    }
}
