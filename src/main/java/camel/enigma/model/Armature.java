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
import camel.enigma.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Armature {

    public static final String UNFIT_ROTORTYPES_MSG = "RotorTypes don't fit other scramblers in armature";
    // R to L
    private static final RotorType[] DEFAULT_ROTOR_TYPES = new RotorType[]{
            HistoricRotorType.III,
            HistoricRotorType.II,
            HistoricRotorType.I};
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

    public ScrambleResult handle(ScrambleResult scrambleResult) {

        click();

        return encrypt(scrambleResult);
    }

    public String scramble(String input) {
        return input.chars()
                .mapToObj(i -> (char) i)
                .map(c -> {
                    // ??
                    String alphabetString = entryWheel.alphabetString;
                    if (!Util.containsChar(alphabetString, c)) {
                        char upperCase = Character.toUpperCase(c);
                        if (Util.containsChar(alphabetString, upperCase)) {
                            c = upperCase;
                        }
                    }
                    click();
                    c = encrypt(c);
                    return c;
                })
                .collect(Collector.of(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append,
                        StringBuilder::toString));
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

    private char encrypt(char c) {
        for (ScramblerMounting scramblerMounting : scramblerWiring) {
            c = scramblerMounting.scramble(c);
        }
        return c;
    }

    private void click() {
        boolean previousNotchEngaged = true;
        for (Rotor rotor : rotors) {
            if (previousNotchEngaged || rotor.isNotchEngaged()) {
                previousNotchEngaged = rotor.click();
            }
        }
    }

    public <T extends ScramblerType<?>> void validateWithCurrent(T scramblerType) throws ArmatureInitException {
        if (scramblerType instanceof EntryWheelType) {
            validateWithCurrent((EntryWheelType) scramblerType);
        } else if (scramblerType instanceof ReflectorType) {
            validateWithCurrent((ReflectorType) scramblerType);
        }
    }

    public void validateWithCurrent(EntryWheelType entryWheelType) throws ArmatureInitException {
        try {
            validateAllTypes(entryWheelType, getRotorTypes(), (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException("EntryWheel doesn't fit other scramblers in armature", e);
        }
    }

    public void validateWithCurrent(RotorType[] rotorTypes) throws ArmatureInitException {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, rotorTypes, (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException(UNFIT_ROTORTYPES_MSG, e);
        }
    }

    public void validateWithCurrent(ReflectorType reflectorType) throws ArmatureInitException {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, getRotorTypes(), reflectorType);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException("Reflector doesn't fit other scramblers in armature", e);
        }
    }

    private void validateWithCurrent(RotorType rotorType, int newPos) throws ArmatureInitException {
        try {
            validateAllTypes((EntryWheelType) entryWheel.type, getRotorTypes(rotorType, newPos), (ReflectorType) reflector.type);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException(String.format("Rotortype at pos %d doesn't fit", newPos), e);
        }
    }

    private void validateAllTypes(EntryWheelType entryWheelType, RotorType[] rotorTypes, ReflectorType reflectorType)
            throws ArmatureInitException {

        ScramblerType<?>[] allTypes = new ScramblerType[rotorTypes.length + 2];
        System.arraycopy(rotorTypes, 0, allTypes, 0 ,rotorTypes.length);
        allTypes[rotorTypes.length] = entryWheelType;
        allTypes[rotorTypes.length + 1] = reflectorType;
        validateAlphabetStrings(allTypes);
    }

    public void setEntryWheel(EntryWheelType entryWheelType) throws ArmatureInitException {
        validateWithCurrent(entryWheelType);
        forceSetEntryWheel(entryWheelType);
    }

    public void forceSetEntryWheel(EntryWheelType entryWheelType) {
        this.entryWheel = initEntryWheel(entryWheelType);
        this.scramblerWiring = initWiring();
    }

    public void setAutoEntryWheel(String alphabetString) {
        this.entryWheel = EntryWheel.auto(alphabetString).freshScrambler();
        this.scramblerWiring = initWiring();
    }

    public void setReflector(ReflectorType reflectorType) throws ArmatureInitException {
        validateWithCurrent(reflectorType);
        forceSetReflector(reflectorType);
    }

    public void forceSetReflector(ReflectorType reflectorType) {
        this.reflector = initReflector(reflectorType);
        this.scramblerWiring = initWiring();
    }

    public void setAutoReflector(String alphabetString) {
        this.reflector = Reflector.auto(alphabetString).freshScrambler();
        this.scramblerWiring = initWiring();
    }

    public void setRotors(RotorType[] types) throws ArmatureInitException {
        validateWithCurrent(types);
        forceSetRotors(types);
    }

    public void forceSetRotors(RotorType[] types) {
        Rotor[] newRotors = initRotors(types);
        tryAndCopyOffsets(newRotors);
        this.rotors = newRotors;
        this.scramblerWiring = initWiring();
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

    private static void validateAlphabetStrings(ScramblerType<?>[] scramblerTypes) throws ArmatureInitException {
        // TODO extract class name of type and format into message
        String prevAlphabetString = null;
        for (ScramblerType<?> scramblerType : scramblerTypes) {
            prevAlphabetString = validateAlphabetString(prevAlphabetString, scramblerType);
        }
    }

    private static String validateAlphabetString(String prevAlphabetString,
                                                 ScramblerType<?> scramblerType) throws ArmatureInitException {
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

    public RotorType[] getRotorTypes() {
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

    public EntryWheelType getEntryWheelType() {
        return (EntryWheelType) entryWheel.getType();
    }

    public ReflectorType getReflectorType() {
        return (ReflectorType) reflector.getType();
    }
}
