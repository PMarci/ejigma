package camel.enigma.model.type;

import camel.enigma.model.historic.HistoricEntryWheelType;
import camel.enigma.model.historic.HistoricReflectorType;
import camel.enigma.model.historic.HistoricRotorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ConfigContainer {

    private List<RotorType> rotorTypes;
    private List<ReflectorType> reflectorTypes;
    private List<EntryWheelType> entryWheelTypes;

    @Autowired
    public ConfigContainer(List<HistoricRotorType> hRotorTypes,
                           @Nullable List<RotorType> rotorTypes,
                           List<HistoricReflectorType> hReflectorTypes,
                           @Nullable List<ReflectorType> reflectorTypes,
                           List<HistoricEntryWheelType> hEntryWheelTypes,
                           @Nullable List<EntryWheelType> entryWheelTypes) {
        this.rotorTypes = new ArrayList<>();
        this.rotorTypes.addAll(hRotorTypes);
        if (rotorTypes != null) {
            this.rotorTypes.addAll(rotorTypes);
        }
        this.reflectorTypes = new ArrayList<>();
        this.reflectorTypes.addAll(hReflectorTypes);
        if (reflectorTypes != null) {
            this.reflectorTypes.addAll(reflectorTypes);
        }
        this.entryWheelTypes =  new ArrayList<>();
        this.entryWheelTypes.addAll(hEntryWheelTypes);
        if (entryWheelTypes != null) {
            this.entryWheelTypes.addAll(entryWheelTypes);
        }
    }

    public <T extends ScramblerType> List<T> getScramblerTypes(Class<T> scramblerType) {
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

    @Bean
    public static List<HistoricRotorType> getHRotorTypes() {
        return getEnumConstants(HistoricRotorType.class);
    }

    @Bean
    public static List<HistoricReflectorType> getHReflectorTypes() {
        return getEnumConstants(HistoricReflectorType.class);
    }

    @Bean
    public static List<HistoricEntryWheelType> getHEntryWheelTypes() {
        return getEnumConstants(HistoricEntryWheelType.class);
    }

    private static <T extends ScramblerType> List<T> getEnumConstants(Class<? extends T> enu) {
        T[] enumConstants = enu.getEnumConstants();
        List<T> result = Collections.emptyList();
        if (enumConstants != null) {
            result = Arrays.asList(enumConstants);
        }
        return result;
    }

}
