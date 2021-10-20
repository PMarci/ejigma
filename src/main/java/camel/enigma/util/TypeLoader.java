package camel.enigma.util;

import camel.enigma.model.type.*;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class TypeLoader {

    private static JAXBContext jaxbContext = initJaxbContext();

    private static String path = getPath();

    public TypeLoader() {

    }

    public static List<RotorType> loadCustomRotorTypes() {
        List<RotorType> rotorTypes = Collections.emptyList();
        List<File> sourceFiles = listFiles();
        try {
            rotorTypes = getCustomRotorTypes(sourceFiles);
        } catch (JAXBException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return rotorTypes;
    }

    public static List<ReflectorType> loadCustomReflectorTypes() {
        // TODO
        return Collections.emptyList();
    }

    public static List<EntryWheelType> loadCustomEntryWheelTypes() {
        // TODO
        return Collections.emptyList();
    }

    private static List<File> listFiles() {
        File dir = new File(path);
        List<File> sourceFiles = Collections.emptyList();
        String[] jsons = dir.list((dir1, name) -> name.endsWith(".json"));
        if (jsons != null) {
            sourceFiles = Arrays.stream(jsons)
                .map(s -> new File(path + File.separator + s))
                .collect(Collectors.toList());
        }
        return sourceFiles;
    }

    private static List<RotorType> getCustomRotorTypes(List<File> sourceFiles) throws JAXBException, FileNotFoundException {
        List<RotorType> result = new ArrayList<>();
        CustomRotorType customRotorType;
        for (File sourceFile : sourceFiles) {
            Unmarshaller unmarshaller = getUnmarshaller();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8);
            StreamSource source = new StreamSource(reader);
            JAXBElement<CustomRotorType> customRotorTypeElem = unmarshaller.unmarshal(source, CustomRotorType.class);
            if (customRotorTypeElem != null) {
                customRotorType = customRotorTypeElem.getValue();
                if (customRotorType != null) {
                    result.add(customRotorType);
                }
            }
        }
        return result;
    }

    private static String getPath() {
        Class<TypeLoader> typeLoaderClass = TypeLoader.class;
        Optional<URL> resourceOpt = Optional.ofNullable(typeLoaderClass.getResource("./"));
        return resourceOpt
                .map(URL::toExternalForm)
                .orElse(typeLoaderClass.getResource("/" + typeLoaderClass.getPackageName().replace(".", "/")).toExternalForm());
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        return unmarshaller;
    }

    private static JAXBContext initJaxbContext() {
        JAXBContext result = null;
        try {
            result = JAXBContext.newInstance(CustomRotorType.class, CustomReflectorType.class, CustomEntryWheelType.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }
}
