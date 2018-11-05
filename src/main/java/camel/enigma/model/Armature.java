package camel.enigma.model;

import camel.enigma.util.ScrambleResult;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class Armature {

    private RotorType[] defaultRotorTypes = new RotorType[]{RotorType.III, RotorType.II, RotorType.I};
    private ReflectorType defaultReflectorType = ReflectorType.B;
    private Rotor[] rotors = initRotors(defaultRotorTypes);
    private Reflector reflector = initReflector(defaultReflectorType);
    private List<Scrambler> scramblerWiring = initWirings(rotors, reflector);

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {
        ScrambleResult current = scrambleResult;
        for (int i = 0; i < scramblerWiring.size(); i++) {
            Scrambler scrambler = scramblerWiring.get(i);
            if (i == 0) {
                scrambler.click();
            }
            current = scrambler.scramble(current);
        }
        return current;
    }

    private Rotor[] initRotors(RotorType[] rotorTypes) {
        return Arrays.stream(rotorTypes).sequential().map(RotorType::getRotor).toArray(Rotor[]::new);
    }

    private Reflector initReflector(ReflectorType reflectorType) {
        return reflectorType.getReflector();
    }

    private List<Scrambler> initWirings(Rotor[] rotors, Reflector reflector) {
        List<Scrambler> resultList = IntStream.range(0, rotors.length * 2 + 1).sequential()
                .mapToObj(value -> {
                    Scrambler result;
                    if (value < rotors.length) {
                        result = rotors[value];
                    } else if (value == rotors.length) {
                        result = reflector;
                    } else {
                        result = rotors[rotors.length * 2 - value];
                    }
                    return result;
                })
                .collect(Collectors.toList());

        return resultList;
    }
}
