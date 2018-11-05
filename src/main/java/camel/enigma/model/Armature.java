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
    private List<ScramblerMounting> scramblerWiring = initWirings(rotors, reflector);

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {
        ScrambleResult current = scrambleResult;
        for (int i = 0; i < scramblerWiring.size(); i++) {
            ScramblerMounting scramblerMounting = scramblerWiring.get(i);
            Scrambler scrambler = scramblerMounting.getScrambler();
            if (i == 0) {
                scrambler.click();
            }
            if (!scramblerMounting.isReverseWired()) {
                current = scrambler.scramble(current);
            } else {
                current = scrambler.reverseScramble(current);
            }
            if (i == scramblerWiring.size() - 1) {
                current.recordOutput();
            }
        }
        return current;
    }

    private Rotor[] initRotors(RotorType[] rotorTypes) {
        return Arrays.stream(rotorTypes).sequential().map(RotorType::getRotor).toArray(Rotor[]::new);
    }

    private Reflector initReflector(ReflectorType reflectorType) {
        return reflectorType.getReflector();
    }

    private List<ScramblerMounting> initWirings(Rotor[] rotors, Reflector reflector) {
        List<ScramblerMounting> resultList = IntStream.range(0, rotors.length * 2 + 1).sequential()
                .mapToObj(value -> {
                    ScramblerMounting result;
                    if (value < rotors.length) {
                        result = new ScramblerMounting(rotors[value]);
                    } else if (value == rotors.length) {
                        result = new ScramblerMounting(reflector);
                    } else {
                        result = new ScramblerMounting(rotors[rotors.length * 2 - value], true);
                    }
                    return result;
                })
                .collect(Collectors.toList());

        return resultList;
    }
}
