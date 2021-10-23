package ejigma.model.type;

import ejigma.model.Scrambler;
import ejigma.model.historic.HistoricEntryWheelType;
import ejigma.model.historic.HistoricPlugBoardConfig;
import ejigma.model.historic.HistoricReflectorType;
import ejigma.model.historic.HistoricRotorType;
import ejigma.util.TypeLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ConfigContainer {

    private final JAXBContext jaxbContext = initJaxbContext();
    private List<RotorType> rotorTypes;
    private List<ReflectorType> reflectorTypes;
    private List<EntryWheelType> entryWheelTypes;
    private List<PlugBoardConfig> customPlugBoardConfigs;
    TypeLoader typeLoader = new TypeLoader(jaxbContext);

    public ConfigContainer() {
        this.entryWheelTypes = initScramblerTypes(
                CustomEntryWheelType.class,
                ConfigContainer::getHEntryWheelTypes,
                TypeLoader.ENTRYWHEEL_TYPES_FOLDER);
        this.rotorTypes = initScramblerTypes(
                CustomRotorType.class,
                ConfigContainer::getHRotorTypes,
                TypeLoader.ROTOR_TYPES_FOLDER);
        this.reflectorTypes = initScramblerTypes(
                CustomReflectorType.class,
                ConfigContainer::getHReflectorTypes,
                TypeLoader.REFLECTOR_TYPES_FOLDER);
        this.customPlugBoardConfigs = initScramblerTypes(
                CustomPlugBoardConfig.class,
                ConfigContainer::getHPlugBoardConfigs,
                TypeLoader.ENTRYWHEEL_TYPES_FOLDER);
    }

    @SuppressWarnings("unchecked")
    private <C extends T, T extends ScramblerType<S>, S extends Scrambler> List<T> initScramblerTypes(
            Class<? extends CustomScramblerType<S>> customScramblerTypeClass,
            Supplier<List<C>> historicSupplier,
            String subFolder) {

        List<ScramblerType<S>> scramblerTypes = new ArrayList<>(historicSupplier.get());
        List<? extends CustomScramblerType<S>> cScramblerTypes =
                typeLoader.loadCustomScramblerTypes(customScramblerTypeClass, subFolder);
        if (!cScramblerTypes.isEmpty()) {
            scramblerTypes.addAll(cScramblerTypes);
        }
        return (List<T>) scramblerTypes;
    }

    private static JAXBContext initJaxbContext() {
        JAXBContext result = null;
        try {
            result = JAXBContext.newInstance(
                    CustomRotorType.class,
                    CustomReflectorType.class,
                    CustomEntryWheelType.class,
                    CustomPlugBoardConfig.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
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

    public JAXBContext getJaxbContext() {
        return jaxbContext;
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

    public List<PlugBoardConfig> getCustomPlugBoardConfigs() {
        return customPlugBoardConfigs;
    }

    public void setCustomPlugBoardConfigs(List<PlugBoardConfig> customPlugBoardConfigs) {
        this.customPlugBoardConfigs = customPlugBoardConfigs;
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

    public static List<HistoricPlugBoardConfig> getHPlugBoardConfigs() {
        return getEnumConstants(HistoricPlugBoardConfig.class);
    }

    private static <S extends Scrambler, T extends ScramblerType<S>> List<T> getEnumConstants(Class<? extends T> enu) {
        T[] enumConstants = enu.getEnumConstants();
        List<T> result = Collections.emptyList();
        if (enumConstants != null) {
            result = Arrays.asList(enumConstants);
        }
        return result;
    }

}
