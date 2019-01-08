package camel.enigma.model;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.model.historic.HistoricEntryWheelType;
import camel.enigma.model.historic.HistoricRotorType;
import camel.enigma.model.type.EntryWheelType;
import camel.enigma.model.type.ReflectorType;
import camel.enigma.model.type.RotorType;
import camel.enigma.model.type.ScramblerType;
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
        new RotorType[] { HistoricRotorType.III, HistoricRotorType.II, HistoricRotorType.I };
//        new RotorType[] { HistoricRotorType.M };
    private static final ReflectorType DEFAULT_REFLECTOR_TYPE =
        Reflector.auto(Scrambler.DEFAULT_ALPHABET_STRING);
//        HistoricReflectorType.C;
    private static final EntryWheelType DEFAULT_ENTRY_WHEEL_TYPE =
//        EntryWheel.auto(Scrambler.DEFAULT_ALPHABET_STRING);
    HistoricEntryWheelType.ENIGMA_I;
    private EntryWheel entryWheel;
    private Rotor[] rotors;
    private Reflector reflector;
    private List<ScramblerMounting> scramblerWiring;

    private String alphabetString;
    private boolean autoEntryWheel = true;
    private boolean autoRandomReflector = true;

    public Armature() throws ArmatureInitException {
        this(DEFAULT_ENTRY_WHEEL_TYPE, DEFAULT_ROTOR_TYPES, DEFAULT_REFLECTOR_TYPE);
    }

    public Armature(EntryWheelType entryWheelType, RotorType[] rotorTypes, ReflectorType reflectorType)
        throws ArmatureInitException {
        validateAllTypes(entryWheelType, rotorTypes, reflectorType);
        // TODO make alphabetString not nullable
        validateEntryWheel(entryWheelType, alphabetString.length());
        entryWheel = initEntryWheel(entryWheelType);
        rotors = initRotors(rotorTypes);
//        validateReflector(reflectorType, alphabetString.length());
        reflector = initReflector(reflectorType);
        scramblerWiring = initWirings(entryWheel, rotors, reflector);
    }

    private boolean validateWithCurrent(RotorType[] rotorTypes) {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, rotorTypes, (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void validateAllTypes(EntryWheelType entryWheelType, RotorType[] rotorTypes, ReflectorType reflectorType)
        throws ArmatureInitException {

        ScramblerType[] allTypes = new ScramblerType[rotorTypes.length + 2];
        for (int i = 0, rotorTypesLength = rotorTypes.length; i < rotorTypesLength; i++) {
            allTypes[i] = rotorTypes[i];
        }
        allTypes[rotorTypes.length] = entryWheelType;
        allTypes[rotorTypes.length + 1] = reflectorType;
        alphabetString = validateAlphabetStrings(allTypes);
    }

    @Handler
    public ScrambleResult handle(@Body ScrambleResult scrambleResult) {

        click();

        return encrypt(scrambleResult);
    }

    public void setRotors(RotorType[] types) throws ArmatureInitException {
        String newAlphabet = validateAlphabetStrings(types);
        if (autoEntryWheel) {
            // TODO message?
            this.entryWheel = EntryWheel.auto(newAlphabet).freshScrambler();
            this.alphabetString = newAlphabet;
        }
        if (autoRandomReflector) {
            // TODO message?
            this.reflector = Reflector.auto(newAlphabet).freshScrambler();
            this.alphabetString = newAlphabet;
        }
        if (validateWithCurrent(types)) {
            Rotor[] newRotors = initRotors(types);
            tryAndCopyOffsets(newRotors);
            this.rotors = newRotors;
            scramblerWiring = initWirings();
        } else {
            throw new ArmatureInitException("scramblerWheels don't fit");
        }
    }

    private void tryAndCopyOffsets(Rotor[] newRotors) {
        if (newRotors != null) {
            for (int i = 0; i < rotors.length && i < newRotors.length; i++) {
                try {
                    newRotors[i].setOffset(rotors[i].getOffsetAsChar());
                } catch (IllegalArgumentException e) {
                    // TODO handle
//                    e.printStackTrace();
                    try {
                        newRotors[i].setOffset(rotors[i].getOffset());

                    } catch (IllegalArgumentException e2) {
                        // TODO handle
//                        e2.printStackTrace();
                        newRotors[i].setOffset(0);
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

    private String validateAlphabetStrings(ScramblerType[] scramblerTypes) throws ArmatureInitException {
        // TODO extract class name of type and format into message
        String prevAlphabetString = null;
        for (ScramblerType scramblerType : scramblerTypes) {
            if (scramblerType != null) {
                String currentAlphabetString = scramblerType.getAlphabetString();
                if (prevAlphabetString != null && !prevAlphabetString.equals(currentAlphabetString)) {
                    throw new ArmatureInitException("scramblers' alphabetStrings differ");
                } else if (prevAlphabetString == null && currentAlphabetString != null) {
                    prevAlphabetString = currentAlphabetString;
                } else if (currentAlphabetString == null) {
                    throw new ArmatureInitException("alphabetString can't be null");
                }
            } else {
                throw new ArmatureInitException("scramblerType can't be null");
            }
        }
        return prevAlphabetString;
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

    // TODO temp
    public Reflector getReflector() {
        return reflector;
    }
}
