package camel.enigma.model;

import camel.enigma.exception.ArmatureInitException;
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

    // R to L
    private static final RotorType[] DEFAULT_ROTOR_TYPES = new RotorType[]{RotorType.III, RotorType.II, RotorType.I};
    private static final ReflectorType DEFAULT_REFLECTOR_TYPE = ReflectorType.B;
    private Rotor[] rotors;
    private Reflector reflector;
    private List<ScramblerMounting> scramblerWiring;

    public Armature() throws ArmatureInitException {
        this(DEFAULT_ROTOR_TYPES, DEFAULT_REFLECTOR_TYPE);
    }

    public Armature(RotorType[] rotorTypes, ReflectorType reflectorType) throws ArmatureInitException {
        int rotorWiringNo = validateRotors(rotorTypes);
        rotors = initRotors(rotorTypes);
        validateReflector(reflectorType, rotorWiringNo);
        reflector = initReflector(reflectorType);
        scramblerWiring = initWirings(rotors, reflector);
    }

    private int validateRotors(RotorType[] rotorTypes) throws ArmatureInitException {
        int result = 0;
        for (RotorType rotorType : rotorTypes) {
            int current = rotorType.getRotor().getWirings().length;
            if (result != 0) {
                if (current != result) {
                    throw new ArmatureInitException("rotor wiring no mismatch");
                }
            } else {
                result = current;
            }
        }
        return result;
    }

    private void validateReflector(ReflectorType reflectorType, int rotorWiringNo) throws ArmatureInitException {
        if (reflectorType.getReflector().getWirings().length != rotorWiringNo) {
            throw new ArmatureInitException("reflector wiring no mismatch");
        }
    }

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {
        ScrambleResult current = scrambleResult;
        click();
        for (int i = 0; i < scramblerWiring.size(); i++) {
            ScramblerMounting scramblerMounting = scramblerWiring.get(i);
            current = scramblerMounting.scramble(current);
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
        return IntStream.range(0, rotors.length * 2 + 1).sequential()
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
    }

    private void click() {
        boolean previousNotchEngaged = true;
        for (Rotor rotor : rotors) {
            if (previousNotchEngaged || rotor.isNotchEngaged()) {
                previousNotchEngaged = rotor.click();
            }
        }
    }
}
