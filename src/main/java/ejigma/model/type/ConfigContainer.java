package ejigma.model.type;

import ejigma.model.historic.HistoricEntryWheelType;
import ejigma.model.historic.HistoricReflectorType;
import ejigma.model.historic.HistoricRotorType;
import ejigma.util.TypeLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigContainer {

    private List<RotorType> rotorTypes;
    private List<ReflectorType> reflectorTypes;
    private List<EntryWheelType> entryWheelTypes;

    public ConfigContainer() {
        this.rotorTypes = new ArrayList<>();
        this.rotorTypes.addAll(getHRotorTypes());
        List<RotorType> cRotorTypes = TypeLoader.loadCustomRotorTypes();
        if (cRotorTypes != null && !cRotorTypes.isEmpty()) {
            this.rotorTypes.addAll(cRotorTypes);
        }
        this.reflectorTypes = new ArrayList<>();
        this.reflectorTypes.addAll(getHReflectorTypes());
        List<ReflectorType> cReflectorTypes = TypeLoader.loadCustomReflectorTypes();
        if (cReflectorTypes != null && !cReflectorTypes.isEmpty()) {
            this.reflectorTypes.addAll(cReflectorTypes);
        }
        this.entryWheelTypes = new ArrayList<>();
        this.entryWheelTypes.addAll(getHEntryWheelTypes());
        List<EntryWheelType> cEntryWheelTypes = TypeLoader.loadCustomEntryWheelTypes();
        if (cEntryWheelTypes != null && !cEntryWheelTypes.isEmpty()) {
            this.entryWheelTypes.addAll(cEntryWheelTypes);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ScramblerType<?>> List<T> getScramblerTypes(Class<T> scramblerType) {
        List<T> result = null;
        if (scramblerType.isAssignableFrom(RotorType.class)) {
            result = (List<T>) getRotorTypes();
        } else if (scramblerType.isAssignableFrom(ReflectorType.class)) {
            result = (List<T>) getReflectorTypes();
        } else if (scramblerType.isAssignableFrom(EntryWheelType.class)) {
            result = (List<T>) getEntryWheelTypes();
        }
        return result;
    }

    public List<RotorType> getRotorTypes() {
        return rotorTypes;
    }

    public void setRotorTypes(List<RotorType> rotorTypes) {
        this.rotorTypes = rotorTypes;
    }

    public List<ReflectorType> getReflectorTypes() {
        return reflectorTypes;
    }

    public void setReflectorTypes(List<ReflectorType> reflectorTypes) {
        this.reflectorTypes = reflectorTypes;
    }

    public List<EntryWheelType> getEntryWheelTypes() {
        return entryWheelTypes;
    }

    public void setEntryWheelTypes(List<EntryWheelType> entryWheelTypes) {
        this.entryWheelTypes = entryWheelTypes;
    }

    public static List<HistoricRotorType> getHRotorTypes() {
        return getEnumConstants(HistoricRotorType.class);
    }

    public static List<HistoricReflectorType> getHReflectorTypes() {
        return getEnumConstants(HistoricReflectorType.class);
    }

    public static List<HistoricEntryWheelType> getHEntryWheelTypes() {
        return getEnumConstants(HistoricEntryWheelType.class);
    }

    private static <T extends ScramblerType<?>> List<T> getEnumConstants(Class<? extends T> enu) {
        T[] enumConstants = enu.getEnumConstants();
        List<T> result = Collections.emptyList();
        if (enumConstants != null) {
            result = Arrays.asList(enumConstants);
        }
        return result;
    }

}
