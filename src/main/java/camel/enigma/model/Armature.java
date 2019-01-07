package camel.enigma.model;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.model.historic.HistoricEntryWheelType;
import camel.enigma.model.historic.HistoricReflectorType;
import camel.enigma.model.historic.HistoricRotorType;
import camel.enigma.model.type.EntryWheelType;
import camel.enigma.model.type.ReflectorType;
import camel.enigma.model.type.RotorType;
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
    private static final RotorType[] DEFAULT_ROTOR_TYPES =
            new RotorType[]{HistoricRotorType.III, HistoricRotorType.II, HistoricRotorType.I};
    private static final ReflectorType DEFAULT_REFLECTOR_TYPE = HistoricReflectorType.B;
    private static final EntryWheelType DEFAULT_ENTRY_WHEEL_TYPE = HistoricEntryWheelType.ENIGMA_I;
    private EntryWheel entryWheel;
    private Rotor[] rotors;
    private Reflector reflector;
    private List<ScramblerMounting> scramblerWiring;

    private String alphabetString;

    public Armature() throws ArmatureInitException {
        this(DEFAULT_ENTRY_WHEEL_TYPE, DEFAULT_ROTOR_TYPES, DEFAULT_REFLECTOR_TYPE);
    }

    public Armature(EntryWheelType entryWheelType, RotorType[] rotorTypes, ReflectorType reflectorType)
            throws ArmatureInitException {
        alphabetString = entryWheelType.getAlphabetString();
        validateEntryWheel(entryWheelType, alphabetString.length());
        entryWheel = initEntryWheel(entryWheelType);
        validateRotors(rotorTypes, alphabetString.length());
        rotors = initRotors(rotorTypes);
        validateReflector(reflectorType, alphabetString.length());
        reflector = initReflector(reflectorType);
        scramblerWiring = initWirings(entryWheel, rotors, reflector);
    }

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {

        click();

        return encrypt(scrambleResult);
    }

    public void setRotors(RotorType[] types) throws ArmatureInitException {
        validateRotors(types);
        Rotor[] newRotors = initRotors(types);
        tryAndCopyOffsets(newRotors);
        this.rotors = newRotors;
        scramblerWiring = initWirings();
    }

    private void tryAndCopyOffsets(Rotor[] newRotors) {
        if (newRotors != null) {
            for (int i = 0; i < rotors.length && i < newRotors.length; i++) {
                try {
                    newRotors[i].setOffset(rotors[i].getOffsetAsChar());
                } catch (IllegalArgumentException e) {
                    // TODO handle
                    e.printStackTrace();
                    try {
                        newRotors[i].setOffset(rotors[i].getOffset());

                    } catch (IllegalArgumentException e2) {
                        // TODO handle
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    public void resetOffsets() {
        for (Rotor rotor : rotors) {
            rotor.setOffset('A');
        }
    }

    public String getOffsetString() {
        StringBuilder sb = new StringBuilder();
        for (Rotor rotor : rotors) {
            sb.append(rotor.offsetAsChar);
        }
        return sb.toString();
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

    public <T extends ScramblerWheel> boolean fits(T scramblerWheel) {
        return alphabetString.equals(scramblerWheel.getAlphabetString());
    }

    private void validateEntryWheel(EntryWheelType entryWheelType, int length) {
        // TODO do other stuff
//        return entryWheelType.freshScrambler().alphabet.length;
    }

    private void validateRotors(RotorType[] rotorTypes) throws ArmatureInitException {
        validateRotors(rotorTypes, alphabetString.length());
    }

    private void validateRotors(RotorType[] rotorTypes, int alphabetLength) throws ArmatureInitException {
        for (RotorType rotorType : rotorTypes) {
            if (rotorType != null) {
                int current = rotorType.getAlphabetString().length();
                if (current != alphabetLength) {
                    throw new ArmatureInitException("rotor wiring no mismatch");
                }
            } else {
                throw new ArmatureInitException("rotor can't be null");
            }
        }
    }

    private void validateReflector(ReflectorType reflectorType,
                                   int rotorWiringNo) throws ArmatureInitException {
        if (reflectorType.getAlphabetString().length() != rotorWiringNo) {
            throw new ArmatureInitException("reflector wiring no mismatch");
        }
    }

    private EntryWheel initEntryWheel(EntryWheelType entryWheelType) {
        return entryWheelType.freshScrambler();
    }

    private Rotor[] initRotors(RotorType[] rotorTypes) {
        return Arrays.stream(rotorTypes).sequential().map(RotorType::freshScrambler).toArray(Rotor[]::new);
    }

    private Reflector initReflector(ReflectorType reflectorType) {
        return reflectorType.freshScrambler();
    }

    private List<ScramblerMounting> initWirings() {
        return initWirings(entryWheel, rotors, reflector);
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
//                        result = null;
                    }
                    return result;
                })
//                .filter(Objects::nonNull)
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
