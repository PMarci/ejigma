package ejigma.util;

import ejigma.exception.TypeLoaderError;
import ejigma.model.type.*;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TypeLoader {

    private static final JAXBContext jaxbContext = initJaxbContext();

    private static final String PATH = getPath();

    private TypeLoader() {
        // hiding
    }

    public static List<RotorType> loadCustomRotorTypes() {
        List<RotorType> rotorTypes = Collections.emptyList();
        try {
        List<File> sourceFiles = listFiles();
            rotorTypes = getCustomRotorTypes(sourceFiles);
        } catch (JAXBException | FileNotFoundException | URISyntaxException e) {
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

    private static List<File> listFiles() throws URISyntaxException {
        File dir = new File(new URI(PATH));
        List<File> sourceFiles = Collections.emptyList();
        String[] jsons = dir.list((dir1, name) -> name.endsWith(".json"));
        if (jsons != null) {
            List<File> list = new ArrayList<>();
            for (String s : jsons) {
                File file = new File(PATH.substring(PATH.indexOf(':') + 1) + File.separator + s);
                    list.add(file);
            }
            sourceFiles = list;
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
                .map(url1 -> url1.toExternalForm())
                .map(s -> typeLoaderClass.getResource(
                        "/" + typeLoaderClass.getPackageName().replace(".", "/")))
                .map(url -> url.toExternalForm())
                .orElseThrow(() -> new TypeLoaderError("Couldn't init TypeLoader!"));
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
            result = JAXBContext.newInstance(
                    CustomRotorType.class,
                    CustomReflectorType.class,
                    CustomEntryWheelType.class
//                    "ejigma.model.type", TypeLoader.class.getClassLoader()
                    );
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }
}
