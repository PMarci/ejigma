package ejigma.model;

import ejigma.exception.ArmatureInitException;
import ejigma.exception.ScramblerSettingException;
import ejigma.model.historic.HistoricEntryWheelType;
import ejigma.model.historic.HistoricReflectorType;
import ejigma.model.historic.HistoricRotorType;
import ejigma.model.type.*;
import ejigma.util.GsonExclude;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Armature implements Printable {

    public static final String UNFIT_SCRAMBLER_MSG_FORMAT = "Selected %s doesn't fit other scramblers in armature";
    // R to L
    public static final RotorType[] DEFAULT_ROTOR_TYPES = new RotorType[]{
            HistoricRotorType.III,
            HistoricRotorType.II,
            HistoricRotorType.I};
    public static final ReflectorType DEFAULT_REFLECTOR_TYPE = HistoricReflectorType.B;
    public static final EntryWheelType DEFAULT_ENTRY_WHEEL_TYPE = HistoricEntryWheelType.ENIGMA_I;

    @GsonExclude
    private final Enigma enigma;
    private final String alphabetString;

    private EntryWheel entryWheel;
    private Rotor[] rotors;
    private Reflector reflector;

    @GsonExclude
    private List<ScramblerMounting<?, ?>> scramblerWiring;

    public Armature(Enigma enigma) throws ArmatureInitException {
        this(enigma, DEFAULT_ENTRY_WHEEL_TYPE, DEFAULT_ROTOR_TYPES, DEFAULT_REFLECTOR_TYPE);
    }

    public Armature(
            Enigma enigma,
            EntryWheelType entryWheelType,
            RotorType[] rotorTypes,
            ReflectorType reflectorType) throws ArmatureInitException {

        this.enigma = enigma;
        validateAllTypes(entryWheelType, rotorTypes, reflectorType);
        this.entryWheel = initEntryWheel(entryWheelType);
        this.alphabetString = entryWheelType.getAlphabetString();
        this.rotors = initRotors(rotorTypes);
        this.reflector = initReflector(reflectorType);
        initWiring();
    }

    void click() {
        boolean previousNotchEngaged = true;
        // TODO setting to enable moving of all rotors
        if (rotors.length <= 3) {
            for (int i = 0, rotorsLength = rotors.length; i < rotorsLength; i++) {
                Rotor rotor = rotors[i];

                boolean notchEngaged = rotor.isNotchEngaged();
                if (previousNotchEngaged || notchEngaged) {
                    // prevent rotor 3 from double-stepping due to its notch being aligned to pall 4
                    // there's no pall 4 on a 3 rotor enigma
                    if (i == 2) {
                        if (previousNotchEngaged) {
                            rotor.click();
                        }
                    } else {
                        rotor.click();
                    }
                    previousNotchEngaged = notchEngaged;
                }
            }
        } else if (rotors.length == 4) {
            for (int i = 0, rotorsLength = rotors.length; i < rotorsLength; i++) {
                Rotor rotor = rotors[i];

                boolean notchEngaged = rotor.isNotchEngaged();
                if (previousNotchEngaged || notchEngaged) {
                    previousNotchEngaged = notchEngaged;
                    // the fourth pall on the 4-rotor navy type doesn't move at all
                    if (i < 3) {
                        rotor.click();
                    }
                }
            }
        } // else relevant when setting available
    }

    public <S extends Scrambler<S, T>, T extends ScramblerType<S, T>> void validateWithCurrent(T scramblerType) throws
                                                                                                                ArmatureInitException {
        if (scramblerType instanceof EntryWheelType) {
            validateWithCurrent((EntryWheelType) scramblerType);
        } else if (scramblerType instanceof ReflectorType) {
            validateWithCurrent((ReflectorType) scramblerType);
        }
    }

    public void validateWithCurrent(EntryWheelType entryWheelType) throws ArmatureInitException {
        try {
            validateAllTypes(entryWheelType, getRotorTypes(), reflector.type);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException(
                    String.format(UNFIT_SCRAMBLER_MSG_FORMAT,
                                  ScramblerType.getScramblerName(entryWheelType.getClass().getSimpleName())),
                    e);
        }
    }

    public void validateWithCurrent(RotorType[] rotorTypes) throws ArmatureInitException {
        try {
            validateAllTypes(entryWheel.type, rotorTypes, reflector.type);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException(
                    String.format(UNFIT_SCRAMBLER_MSG_FORMAT,
                                  ScramblerType.getScramblerName(rotorTypes[0].getClass().getSimpleName())),
                    e);
        }
    }

    public void validateWithCurrent(ReflectorType reflectorType) throws ArmatureInitException {
        try {
            validateAllTypes(entryWheel.type, getRotorTypes(), reflectorType);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException("Reflector doesn't fit other scramblers in armature", e);
        }
    }

    private void validateWithCurrent(RotorType rotorType, int newPos) throws ArmatureInitException {
        try {
            validateAllTypes(entryWheel.type, getRotorTypes(rotorType, newPos), reflector.type);
        } catch (ArmatureInitException e) {
            throw new ArmatureInitException(String.format("Rotortype at pos %d doesn't fit", newPos), e);
        }
    }

    public static void validateRotorTypes(
            RotorType[] rotorTypes) throws ArmatureInitException {

        ScramblerType<?, ?>[] allTypes = new ScramblerType[rotorTypes.length + 2];
        System.arraycopy(rotorTypes, 0, allTypes, 0, rotorTypes.length);
        validateAlphabetStrings(allTypes);
    }

    public static void validateAllTypes(
            EntryWheelType entryWheelType,
            RotorType[] rotorTypes,
            ReflectorType reflectorType) throws ArmatureInitException {

        ScramblerType<?, ?>[] allTypes = new ScramblerType[rotorTypes.length + 2];
        System.arraycopy(rotorTypes, 0, allTypes, 0, rotorTypes.length);
        allTypes[rotorTypes.length] = entryWheelType;
        allTypes[rotorTypes.length + 1] = reflectorType;
        validateAlphabetStrings(allTypes);

    }

    public static void validateAllTypes(
            EntryWheelType entryWheelType,
            RotorType[] rotorTypes,
            ReflectorType reflectorType,
            PlugBoardConfig plugBoardConfig) throws ArmatureInitException {

        ScramblerType<?, ?>[] allTypes = new ScramblerType[rotorTypes.length + 3];
        System.arraycopy(rotorTypes, 0, allTypes, 0, rotorTypes.length);
        allTypes[rotorTypes.length] = entryWheelType;
        allTypes[rotorTypes.length + 1] = reflectorType;
        allTypes[rotorTypes.length + 2] = plugBoardConfig;
        validateAlphabetStrings(allTypes);
    }

    public void setEntryWheel(EntryWheelType entryWheelType) throws ArmatureInitException {
        validateWithCurrent(entryWheelType);
        forceSetEntryWheel(entryWheelType);
    }

    public void forceSetEntryWheel(EntryWheelType entryWheelType) {
        forceSetEntryWheel(initEntryWheel(entryWheelType));
    }

    public void forceSetEntryWheel(EntryWheel entryWheel) {
        this.entryWheel = entryWheel;
        initWiring();
    }

    public void setAutoEntryWheel(String alphabetString) {
        this.entryWheel = EntryWheel.auto(alphabetString).freshScrambler();
        initWiring();
    }

    public void setReflector(ReflectorType reflectorType) throws ArmatureInitException {
        validateWithCurrent(reflectorType);
        forceSetReflector(reflectorType);
    }

    public void forceSetReflector(ReflectorType reflectorType) {
        forceSetReflector(initReflector(reflectorType));
    }

    public void forceSetReflector(Reflector reflector) {
        this.reflector = reflector;
        initWiring();
    }

    public void setAutoReflector(String alphabetString) {
        this.reflector = Reflector.auto(alphabetString).freshScrambler();
        initWiring();
    }

    public void setRotors(RotorType[] types) throws ArmatureInitException {
        validateWithCurrent(types);
        forceSetRotors(types);
    }

    public void forceSetRotors(RotorType[] types) {
        forceSetRotors(initRotors(types));
    }

    public void forceSetRotors(Rotor[] rotors) {
        tryAndCopyOffsets(rotors);
        this.rotors = rotors;
        initWiring();
    }

    // TODO if I implement this state copying for all components I can get rid of the overloads above
    private void tryAndCopyOffsets(Rotor[] newRotors) {
        if (newRotors != null) {
            for (int i = 0; i < rotors.length && i < newRotors.length; i++) {
                try {
                    newRotors[i].setOffset(rotors[i].getOffsetAsChar());
                } catch (IllegalArgumentException | NullPointerException ignored) {
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
        for (int i = rotors.length - 1; i > -1; i--) {
            Rotor rotor = rotors[i];
            sb.append(rotor.offsetAsChar);
        }
        return sb.toString();
    }

    public void setOffsets(String offsetString) throws ScramblerSettingException {
        if (offsetString.length() != rotors.length) {
            throw new ScramblerSettingException("Offset String length and rotor numbers differ");
        } else {
            IntStream.range(0, rotors.length)
                    .forEach(i -> rotors[i].setOffset(offsetString.charAt(rotors.length - 1 - i)));
        }
    }

    private static void validateAlphabetStrings(ScramblerType<?, ?>[] scramblerTypes) throws ArmatureInitException {
        // TODO extract class name of type and format into message
        String prevAlphabetString = null;
        for (ScramblerType<?, ?> scramblerType : scramblerTypes) {
            prevAlphabetString = validateAlphabetString(prevAlphabetString, scramblerType);
        }
    }

    private static String validateAlphabetString(String prevAlphabetString,
                                                 ScramblerType<?, ?> scramblerType) throws ArmatureInitException {
        if (scramblerType != null) {
            String currentAlphabetString = scramblerType.getAlphabetString();
            if (prevAlphabetString != null && !prevAlphabetString.equals(currentAlphabetString)) {
                throw new ArmatureInitException("Selected scramblers' alphabetStrings differ!");
            } else if (prevAlphabetString == null && currentAlphabetString != null) {
                prevAlphabetString = currentAlphabetString;
            } else if (currentAlphabetString == null) {
                throw new ArmatureInitException("The alphabetString can't be null!");
            }
        } else {
            throw new ArmatureInitException("The ScramblerType can't be null!");
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

    private void initWiring() {
        // + 1 for exclusive
        // + 2 for 2x entryWheel
        int endExclusive = rotors.length * 2 + 3;
        this.scramblerWiring = IntStream.range(0, endExclusive).sequential()
                .mapToObj(value -> {
                    ScramblerMounting<?, ?> result;
                    if (value == 0) {
                        result = new ScramblerMounting<>(entryWheel);
                    } else if (value < rotors.length + 1) {
                        result = new ScramblerMounting<>(rotors[value - 1]);
                    } else if (value == rotors.length + 1) {
                        result = new ScramblerMounting<>(reflector);
                    } else if (value != endExclusive - 1) {
                        result = new ScramblerMounting<>(rotors[rotors.length * 2 + 1 - value], true);
                    } else {
                        result = new ScramblerMounting<>(entryWheel, true);
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
                presentTypes[i] = (i == newPos) ? newType : rotors[i].type;
            }
        } else {
            throw new ArmatureInitException("invalid rotor number");
        }
        return presentTypes;
    }

    public EntryWheelType getEntryWheelType() {
        return entryWheel.getType();
    }

    public ReflectorType getReflectorType() {
        return reflector.getType();
    }

    public List<ScramblerMounting<?, ?>> getScramblerWiring() {
        return scramblerWiring;
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    public EntryWheel getEntryWheel() {
        return entryWheel;
    }

    public Rotor[] getRotors() {
        return rotors;
    }

    public Reflector getReflector() {
        return reflector;
    }

    @Override
    public String toString() {
        return print();
    }
}
