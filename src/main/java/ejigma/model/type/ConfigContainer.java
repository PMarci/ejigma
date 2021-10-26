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
                EntryWheelType.class,
                ConfigContainer::getHEntryWheelTypes,
                TypeLoader.ENTRYWHEEL_TYPES_FOLDER);
        this.rotorTypes = initScramblerTypes(
                RotorType.class,
                ConfigContainer::getHRotorTypes,
                TypeLoader.ROTOR_TYPES_FOLDER);
        this.reflectorTypes = initScramblerTypes(
                ReflectorType.class,
                ConfigContainer::getHReflectorTypes,
                TypeLoader.REFLECTOR_TYPES_FOLDER);
        this.customPlugBoardConfigs = initScramblerTypes(
                PlugBoardConfig.class,
                ConfigContainer::getHPlugBoardConfigs,
                TypeLoader.PLUGBOARD_CONFIGS_FOLDER);
    }

    private <T extends ScramblerType<S, T>, S extends Scrambler<S, T>> List<T> initScramblerTypes(
            Class<T> customScramblerTypeClass,
            Supplier<List<T>> historicSupplier,
            String subFolder) {

        List<T> scramblerTypes = new ArrayList<>(historicSupplier.get());
        List<T> cScramblerTypes =
                typeLoader.loadCustomScramblerTypes(customScramblerTypeClass, subFolder);
        if (!cScramblerTypes.isEmpty()) {
            scramblerTypes.addAll(cScramblerTypes);
        }
        return scramblerTypes;
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
    public <S extends Scrambler<S, T>, T extends ScramblerType<S, T>> List<T> getScramblerTypes(Class<T> scramblerTypeClass) {
        List<T> result = null;
        if (scramblerTypeClass.isAssignableFrom(RotorType.class)) {
            result = (List<T>) getRotorTypes();
        } else if (scramblerTypeClass.isAssignableFrom(ReflectorType.class)) {
            result = (List<T>) getReflectorTypes();
        } else if (scramblerTypeClass.isAssignableFrom(EntryWheelType.class)) {
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

    public static List<RotorType> getHRotorTypes() {
        return getEnumConstants(HistoricRotorType.class);
    }

    public static List<ReflectorType> getHReflectorTypes() {
        return getEnumConstants(HistoricReflectorType.class);
    }

    public static List<EntryWheelType> getHEntryWheelTypes() {
        return getEnumConstants(HistoricEntryWheelType.class);
    }

    public static List<PlugBoardConfig> getHPlugBoardConfigs() {
        return getEnumConstants(HistoricPlugBoardConfig.class);
    }

    private static <H extends T,S extends Scrambler<S,T>, T extends ScramblerType<S, T>> List<H> getEnumConstants(Class<? extends H> enu) {
        H[] enumConstants = enu.getEnumConstants();
        List<H> result = Collections.emptyList();
        if (enumConstants != null) {
            result = Arrays.asList(enumConstants);
        }
        return result;
    }

}
