package camel.enigma.model;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.exception.ScramblerSettingException;
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
    private static final EntryWheelType DEFAULT_ENTRY_WHEEL_TYPE = EntryWheelType.ENIGMA_I;
    private EntryWheel entryWheel;
    private Rotor[] rotors;
    private Reflector reflector;
    private List<ScramblerMounting> scramblerWiring;

    public Armature() throws ArmatureInitException, ScramblerSettingException {
        this(DEFAULT_ENTRY_WHEEL_TYPE, DEFAULT_ROTOR_TYPES, DEFAULT_REFLECTOR_TYPE);
    }

    public Armature(EntryWheelType entryWheelType, RotorType[] rotorTypes, ReflectorType reflectorType)
            throws ArmatureInitException, ScramblerSettingException {
        int alphabetLength = validateEntryWheel(entryWheelType);
        entryWheel = initEntryWheel(entryWheelType);
        validateRotors(rotorTypes, alphabetLength);
        rotors = initRotors(rotorTypes);
        validateReflector(reflectorType, alphabetLength);
        reflector = initReflector(reflectorType);
        scramblerWiring = initWirings(entryWheel, rotors, reflector);
    }

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {

        click();

        return encrypt(scrambleResult);
    }

    public void resetOffsets() {
        for (Rotor rotor : rotors) {
            rotor.setOffset('A');
        }
    }

    private ScrambleResult encrypt(ScrambleResult current) {
        for (int i = 0; i < scramblerWiring.size(); i++) {
            ScramblerMounting scramblerMounting = scramblerWiring.get(i);
            current = scramblerMounting.scramble(current);
            if (i == scramblerWiring.size() - 1) {
                current.recordOutput();
            }
        }
        return current;
    }

    private int validateEntryWheel(EntryWheelType entryWheelType) throws ScramblerSettingException {
        // TODO do other stuff
        return entryWheelType.freshScrambler().alphabet.length;
    }

    private void validateRotors(RotorType[] rotorTypes,
                                int alphabetLength) throws ArmatureInitException, ScramblerSettingException {
        for (RotorType rotorType : rotorTypes) {
            int current = rotorType.freshScrambler().alphabet.length;
            if (current != alphabetLength) {
                throw new ArmatureInitException("rotor wiring no mismatch");
            }
        }
    }

    private void validateReflector(ReflectorType reflectorType,
                                   int rotorWiringNo) throws ArmatureInitException, ScramblerSettingException {
        if (reflectorType.freshScrambler().alphabet.length != rotorWiringNo) {
            throw new ArmatureInitException("reflector wiring no mismatch");
        }
    }

    private EntryWheel initEntryWheel(EntryWheelType entryWheelType) throws ScramblerSettingException {
        return entryWheelType.freshScrambler();
    }

    private Rotor[] initRotors(RotorType[] rotorTypes) {
        return Arrays.stream(rotorTypes).sequential().map(rotorType -> {
            try {
                return rotorType.freshScrambler();
            } catch (ScramblerSettingException e) {
                // TODO fix
                e.printStackTrace();
                return null;
            }
        }).toArray(Rotor[]::new);
    }

    private Reflector initReflector(ReflectorType reflectorType) throws ScramblerSettingException {
        return reflectorType.freshScrambler();
    }

    private List<ScramblerMounting> initWirings(EntryWheel entryWheel, Rotor[] rotors, Reflector reflector) {
        // + 1 for exclusive
        // + 2 for 2x entryWheel
        int endExclusive = rotors.length * 2 + 3;
        return IntStream.range(0, endExclusive).sequential()
                .mapToObj(value -> {
                    ScramblerMounting result;
                    if (value == 0) {
                        result = new ScramblerMounting(entryWheel);
                    } else if (value < rotors.length + 1) {
                        result = new ScramblerMounting(rotors[value - 1]);
                    } else if (value == rotors.length + 1) {
                        result = new ScramblerMounting(reflector);
                    } else if (value != endExclusive - 1) {
                        result = new ScramblerMounting(rotors[rotors.length * 2 + 1 - value], true);
                    } else {
                        result = new ScramblerMounting(entryWheel, true);
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
