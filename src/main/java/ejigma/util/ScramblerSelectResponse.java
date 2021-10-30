package ejigma.util;

import ejigma.exception.ArmatureInitException;
import ejigma.model.component.Armature;
import ejigma.model.component.Enigma;
import ejigma.model.type.*;

import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

@SuppressWarnings("unchecked")
public class ScramblerSelectResponse implements Iterator<Class<? extends ScramblerType<?, ?>>> {

    private final String alphabetString;
    private final Deque<Class<? extends ScramblerType<?, ?>>> toSelect;

    private EntryWheelType entryWheelType = null;
    private final RotorType[] rotorTypes;
    private ReflectorType reflectorType = null;
    private PlugBoardConfig plugBoardConfig = null;


    public ScramblerSelectResponse(ScramblerType<?, ?> type, String oldAlphabetString, int rotors) {
        this.toSelect = initAllTypes(rotors);
        this.rotorTypes = new RotorType[rotors];
        if (type != null) {
            if (type instanceof EntryWheelType) {
                EntryWheelType vEntryWheelType = (EntryWheelType) type;
                this.alphabetString = vEntryWheelType.getAlphabetString();
                set(vEntryWheelType, oldAlphabetString, true);
            } else if (type instanceof RotorType) {
                RotorType vRotorType = (RotorType) type;
                this.alphabetString = type.getAlphabetString();
                set(vRotorType, oldAlphabetString);
            } else if (type instanceof ReflectorType) {
                ReflectorType vReflectorType = (ReflectorType) type;
                this.alphabetString = vReflectorType.getAlphabetString();
                set(vReflectorType, oldAlphabetString, true);
            } else if (type instanceof PlugBoardConfig) {
                PlugBoardConfig vPlugBoardConfig = (PlugBoardConfig) type;
                this.alphabetString = vPlugBoardConfig.getAlphabetString();
                set(vPlugBoardConfig, oldAlphabetString, true);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static Deque<Class<? extends ScramblerType<?, ?>>> initAllTypes(int rotors) {
        Deque<Class<? extends ScramblerType<?, ?>>> deque = new LinkedBlockingDeque<>();
        for (int i = 0; i < rotors; i++) {
            deque.add(RotorType.class);
        }
        deque.add(EntryWheelType.class);
        deque.add(ReflectorType.class);
        deque.add(PlugBoardConfig.class);
        return deque;
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    public void validateNonNullTypes() throws ArmatureInitException {
        int extraSpace = 0;
        if (entryWheelType != null) {
            extraSpace++;
        }
        if (reflectorType != null) {
            extraSpace++;
        }
        if (plugBoardConfig != null) {
            extraSpace++;
        }
        ScramblerType<?, ?>[] nonNullRotors = Arrays.stream(rotorTypes)
                .filter(Objects::nonNull)
                .toArray(ScramblerType[]::new);
        ScramblerType<?, ?>[] toValidate = new ScramblerType[nonNullRotors.length + extraSpace];
        System.arraycopy(nonNullRotors, 0, toValidate, 0, nonNullRotors.length);
        if (plugBoardConfig != null) {
            toValidate[nonNullRotors.length + --extraSpace] = plugBoardConfig;
        }
        if (reflectorType != null) {
            toValidate[nonNullRotors.length + --extraSpace] = reflectorType;
        }
        if (entryWheelType != null) {
            toValidate[nonNullRotors.length + --extraSpace] = entryWheelType;
        }
        Armature.validateAlphabetStrings(toValidate);
    }

    private void select(ScramblerType<?, ?> scramblerType, String oldAlphabetString, boolean init) {
        Class<ScramblerType<?, ?>> scramblerTypeClass = (Class<ScramblerType<?, ?>>) scramblerType.getClass();
        // removes all if no change in alphabet
        // the assumption being that the armature can only contain scramblers with the same alphabet
        if (alphabetString.equals(oldAlphabetString)) {
            // exception for rotor choice
            if (RotorType.class.isAssignableFrom(scramblerTypeClass)) {
                // only remove other types, assumes there is only one of each of those
                toSelect.removeIf(scrTypeClass -> scrTypeClass != RotorType.class);
            } else {
                // nothing to reselect
                toSelect.clear();
            }
        }
        if (init) {
            // remove one if this is the constructor call
            toSelect.stream()
                    .filter(scrTypeClass -> scrTypeClass.isAssignableFrom(scramblerTypeClass))
                    .findFirst()
                    .ifPresent(toSelect::remove);
        }
    }

    @Override
    public boolean hasNext() {
        return toSelect.peek() != null;
    }

    @Override
    public Class<? extends ScramblerType<?, ?>> next() {
        return toSelect.remove();
    }

    public void reselect(ScramblerType<?, ?> scramblerType) {
        if (scramblerType instanceof EntryWheelType) {
            toSelect.addFirst(EntryWheelType.class);
            this.entryWheelType = null;
        } else if (scramblerType instanceof ReflectorType) {
            toSelect.addFirst(ReflectorType.class);
            this.reflectorType = null;
        } else if (scramblerType instanceof PlugBoardConfig) {
            toSelect.addFirst(PlugBoardConfig.class);
            this.plugBoardConfig = null;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void reselect(int ind) {
        if (-1 < ind && ind < rotorTypes.length) {
            this.rotorTypes[ind] = null;
            toSelect.addFirst(RotorType.class);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void apply(Enigma enigma) {
        if (entryWheelType != null) {
            enigma.forceSetEntryWheel(entryWheelType);
        }
        if (rotorTypes != null && Util.allNonNulls(rotorTypes)) {
            enigma.forceSetRotors(rotorTypes);
        }
        if (reflectorType != null) {
            enigma.forceSetReflector(reflectorType);
        }
        if (plugBoardConfig != null) {
            enigma.forceSetPlugBoard(plugBoardConfig);
        }
    }

    public void set(ScramblerType<?, ?> type, String oldAlphabetString) {
        // no rotortypes here, maybe public overload is better
        if (type != null) {
            if (type instanceof EntryWheelType) {
                EntryWheelType type1 = (EntryWheelType) type;
                set(type1, oldAlphabetString, false);
            } else if (type instanceof ReflectorType) {
                ReflectorType type1 = (ReflectorType) type;
                set(type1, oldAlphabetString, false);
            } else if (type instanceof PlugBoardConfig) {
                PlugBoardConfig type1 = (PlugBoardConfig) type;
                set(type1, oldAlphabetString, false);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void set(RotorType rotorType, int ind, String oldAlphabetString) {
        set(rotorType, ind, oldAlphabetString, false);
    }

    private void set(RotorType rotorType, String oldAlphabetString) {
        set(rotorType, 0, oldAlphabetString, true);
    }

    private void set(EntryWheelType entryWheelType, String oldAlphabetString, boolean init) {
        this.entryWheelType = entryWheelType;
        select(entryWheelType, oldAlphabetString, init);
    }

    private void set(RotorType rotorType, int ind, String oldAlphabetString, boolean init) {
        this.rotorTypes[ind] = rotorType;
        select(rotorType, oldAlphabetString, init);
    }

    private void set(ReflectorType reflectorType, String oldAlphabetString, boolean init) {
        this.reflectorType = reflectorType;
        select(reflectorType, oldAlphabetString, init);
    }

    private void set(PlugBoardConfig plugBoardConfig, String oldAlphabetString, boolean init) {
        this.plugBoardConfig = plugBoardConfig;
        select(plugBoardConfig, oldAlphabetString, init);
    }
}
