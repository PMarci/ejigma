package camel.enigma.model;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.model.historic.HistoricEntryWheelType;
import camel.enigma.model.historic.HistoricReflectorType;
import camel.enigma.model.historic.HistoricRotorType;
import camel.enigma.model.type.EntryWheelType;
import camel.enigma.model.type.ReflectorType;
import camel.enigma.model.type.RotorType;
import camel.enigma.model.type.ScramblerType;
import camel.enigma.util.ScrambleResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class Armature {

    // R to L
    private static final RotorType[] DEFAULT_ROTOR_TYPES = new RotorType[] {
        HistoricRotorType.III,
        HistoricRotorType.II,
        HistoricRotorType.I };
    private static final ReflectorType DEFAULT_REFLECTOR_TYPE = HistoricReflectorType.B;
    private static final EntryWheelType DEFAULT_ENTRY_WHEEL_TYPE = HistoricEntryWheelType.ENIGMA_I;

    private EntryWheel entryWheel;
    private Rotor[] rotors;
    private Reflector reflector;
    private List<ScramblerMounting> scramblerWiring;

    public Armature() throws ArmatureInitException {
        this(DEFAULT_ENTRY_WHEEL_TYPE, DEFAULT_ROTOR_TYPES, DEFAULT_REFLECTOR_TYPE);
    }

    public Armature(EntryWheelType entryWheelType, RotorType[] rotorTypes, ReflectorType reflectorType)
        throws ArmatureInitException {
        validateAllTypes(entryWheelType, rotorTypes, reflectorType);
        entryWheel = initEntryWheel(entryWheelType);
        rotors = initRotors(rotorTypes);
        reflector = initReflector(reflectorType);
        scramblerWiring = initWiring();
    }

//    @Handler
    public ScrambleResult handle(/*@Body*/ ScrambleResult scrambleResult) {

        click();

        return encrypt(scrambleResult);
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

    private void click() {
        boolean previousNotchEngaged = true;
        for (Rotor rotor : rotors) {
            if (previousNotchEngaged || rotor.isNotchEngaged()) {
                previousNotchEngaged = rotor.click();
            }
        }
    }

    public boolean validateWithCurrent(EntryWheelType entryWheelType) {
        try {
            validateAllTypes(entryWheelType, getRotorTypes(), (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean validateWithCurrent(RotorType[] rotorTypes) {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, rotorTypes, (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean validateWithCurrent(RotorType rotorType, int newPos) {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, getRotorTypes(rotorType, newPos), (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean validateWithCurrent(ReflectorType reflectorType) {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, getRotorTypes(), reflectorType);
        } catch (ArmatureInitException e) {
//            e.printStackTrace();
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
        validateAlphabetStrings(allTypes);
    }

    public void setEntryWheel(EntryWheelType entryWheelType) {
        setEntryWheel(entryWheelType, false);
    }

    public void setEntryWheel(EntryWheelType entryWheelType, boolean force) {
        if (!force) {
            validateWithCurrent(entryWheelType);
        }

        this.entryWheel = initEntryWheel(entryWheelType);
        this.scramblerWiring = initWiring();
    }

    public void setRotors(RotorType[] types, boolean autoEntryWheel, boolean autoRandomReflector) throws ArmatureInitException {
        String newAlphabet = validateAlphabetStrings(types);
        if (autoEntryWheel) {
            // TODO message?
            this.entryWheel = EntryWheel.auto(newAlphabet).freshScrambler();
        }
        // TODO remove extra cond after finishing prompt for reflector
        if ("AUTO_REFLECTOR".equals(reflector.type.getName()) || autoRandomReflector) {

            // TODO message?
            this.reflector = Reflector.auto(newAlphabet).freshScrambler();
        }
        if (validateWithCurrent(types)) {
            Rotor[] newRotors = initRotors(types);
            tryAndCopyOffsets(newRotors);
            this.rotors = newRotors;
            this.scramblerWiring = initWiring();
        } else {
            throw new ArmatureInitException("rotors don't fit");
        }
    }

    private void tryAndCopyOffsets(Rotor[] newRotors) {
        if (newRotors != null) {
            for (int i = 0; i < rotors.length && i < newRotors.length; i++) {
                try {
                    newRotors[i].setOffset(rotors[i].getOffsetAsChar());
                } catch (IllegalArgumentException ignored) {
                    try {
                        newRotors[i].setOffset(rotors[i].getOffset());

                    } catch (IllegalArgumentException ignoredToo) {
                        // TODO msg?
                        newRotors[i].setOffset(0);
                    }
                }
            }
        }
    }

    public void resetOffsets() {
        for (Rotor rotor : rotors) {
            rotor.setOffset(0);
        }
    }

    public String getOffsetString() {
        StringBuilder sb = new StringBuilder();
        for (Rotor rotor : rotors) {
            sb.append(rotor.offsetAsChar);
        }
        return sb.toString();
    }

    private static String validateAlphabetStrings(ScramblerType[] scramblerTypes) throws ArmatureInitException {
        // TODO extract class name of type and format into message
        String prevAlphabetString = null;
        for (ScramblerType scramblerType : scramblerTypes) {
            prevAlphabetString = validateAlphabetString(prevAlphabetString, scramblerType);
        }
        return prevAlphabetString;
    }

    private static String validateAlphabetString(String prevAlphabetString, ScramblerType scramblerType) throws ArmatureInitException {
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
        return prevAlphabetString;
    }

    private void validateEntryWheel(EntryWheelType entryWheelType) {
        // TODO maybe populate
    }

    private void validateRotor(RotorType rotorType) {
        // TODO maybe populate
    }

    private void validateReflector(ReflectorType reflectorType) {
        // TODO maybe populate
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

    private List<ScramblerMounting> initWiring() {
        return initWiring(entryWheel, rotors, reflector);
    }

    private static List<ScramblerMounting> initWiring(EntryWheel entryWheel, Rotor[] rotors, Reflector reflector) {
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

    private RotorType[] getRotorTypes() {
        return Arrays.stream(rotors).map(rotor -> rotor.type).toArray(RotorType[]::new);
    }

    private RotorType[] getRotorTypes(RotorType newType, int newPos) throws ArmatureInitException {
        RotorType[] presentTypes = new RotorType[rotors.length];
        if (0 < newPos && newPos < rotors.length) {
            for (int i = 0; i < rotors.length; i++) {
                presentTypes[i] = (i == newPos) ? newType : (RotorType) rotors[i].type;
            }
        } else {
            throw new ArmatureInitException("invalid rotor number");
        }
        return presentTypes;
    }
}
