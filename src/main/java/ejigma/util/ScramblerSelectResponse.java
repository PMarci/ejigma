package ejigma.util;

import ejigma.model.Enigma;
import ejigma.model.type.*;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

@SuppressWarnings("unchecked")
public class ScramblerSelectResponse implements Iterator<Class<? extends ScramblerType<?, ?>>> {

    private static final Deque<Class<? extends ScramblerType<?, ?>>> ALL_TYPES = initAllTypes();

    private final String alphabetString;
    private final Deque<Class<? extends ScramblerType<?, ?>>> toSelect = ALL_TYPES;
    private final Deque<? extends ScramblerType<?, ?>> selected = new LinkedBlockingDeque<>();
    private final Iterator<Class<? extends ScramblerType<?, ?>>> toSelectIterator = toSelect.iterator();

    private EntryWheelType entryWheelType = null;
    private RotorType[] rotorTypes = null;
    private ReflectorType reflectorType = null;
    private PlugBoardConfig plugBoardConfig = null;


    public ScramblerSelectResponse(ScramblerType<?, ?> type, String oldAlphabetString, int rotors) {
        if (type != null) {
            if (type instanceof EntryWheelType) {
                EntryWheelType vEntryWheelType = (EntryWheelType) type;
                this.alphabetString = vEntryWheelType.getAlphabetString();
                set(vEntryWheelType, oldAlphabetString);
            } else if (type instanceof RotorType) {
                this.rotorTypes = new RotorType[rotors];
                RotorType vRotorType = (RotorType) type;
                this.alphabetString = type.getAlphabetString();
                set(vRotorType, oldAlphabetString);
            } else if (type instanceof ReflectorType) {
                ReflectorType vReflectorType = (ReflectorType) type;
                this.alphabetString = vReflectorType.getAlphabetString();
                set(vReflectorType, oldAlphabetString);
            } else if (type instanceof PlugBoardConfig) {
                PlugBoardConfig vPlugBoardConfig = (PlugBoardConfig) type;
                this.alphabetString = vPlugBoardConfig.getAlphabetString();
                set(vPlugBoardConfig, oldAlphabetString);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static Deque<Class<? extends ScramblerType<?, ?>>> initAllTypes() {
        Deque<Class<? extends ScramblerType<?, ?>>> deque = new LinkedBlockingDeque<>();
        deque.add(EntryWheelType.class);
        deque.add(RotorType.class);
        deque.add(ReflectorType.class);
        deque.add(PlugBoardConfig.class);
        return deque;
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    public RotorType[] getRotorTypes() {
        return rotorTypes;
    }

    public void reselectOthers(ScramblerType<?, ?> scramblerType, String oldAlphabetString) {
        Class<ScramblerType<?, ?>> scramblerTypeClass = (Class<ScramblerType<?, ?>>) scramblerType.getClass();
        toSelect.removeIf(toSelectCls ->
                                  toSelectCls.isAssignableFrom(scramblerTypeClass) ||
                                          oldAlphabetString.equals(scramblerType.getAlphabetString()));
    }

    @Override
    public boolean hasNext() {
        return toSelectIterator.hasNext();
    }

    @Override
    public Class<? extends ScramblerType<?, ?>> next() {
        return toSelectIterator.next();
    }

    public void reselect(Class<? extends ScramblerType<?, ?>> scramblerTypeClass) {
        if (scramblerTypeClass.isAssignableFrom(EntryWheelType.class)) {
            toSelect.add(scramblerTypeClass);
            // not used
        } else if (scramblerTypeClass.isAssignableFrom(RotorType.class)) {
            toSelect.add(RotorType.class);
        } else if (scramblerTypeClass.isAssignableFrom(ReflectorType.class)) {
            toSelect.add(ReflectorType.class);
        } else if (scramblerTypeClass.isAssignableFrom(PlugBoardConfig.class)) {
            toSelect.add(PlugBoardConfig.class);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void apply(Enigma enigma) {
        if (entryWheelType != null) {
            enigma.forceSetEntryWheel(entryWheelType);
        }
        if (rotorTypes != null && rotorTypes.length > 0) {
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
        if (type != null) {
            if (type instanceof EntryWheelType) {
                EntryWheelType type1 = (EntryWheelType) type;
                set(type1, oldAlphabetString);
            } else if (type instanceof RotorType) {
                RotorType type1 = (RotorType) type;
                set(type1, oldAlphabetString);
            } else if (type instanceof ReflectorType) {
                ReflectorType type1 = (ReflectorType) type;
                set(type1, oldAlphabetString);
            } else if (type instanceof PlugBoardConfig) {
                PlugBoardConfig type1 = (PlugBoardConfig) type;
                set(type1, oldAlphabetString);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void set(EntryWheelType entryWheelType, String oldAlphabetString) {
        this.entryWheelType = entryWheelType;
        reselectOthers(entryWheelType, oldAlphabetString);
    }

    private void set(RotorType rotorTypes, String oldAlphabetString) {
        set(rotorTypes, 0, oldAlphabetString);
    }

    public void set(RotorType rotorType, int pos, String oldAlphabetString) {
        this.rotorTypes[pos] = rotorType;
        reselectOthers(rotorType, oldAlphabetString);
    }

    private void set(ReflectorType reflectorType, String oldAlphabetString) {
        this.reflectorType = reflectorType;
        reselectOthers(reflectorType, oldAlphabetString);
    }

    private void set(PlugBoardConfig plugBoardConfig, String oldAlphabetString) {
        this.plugBoardConfig = plugBoardConfig;
        reselectOthers(plugBoardConfig, oldAlphabetString);
    }
}
