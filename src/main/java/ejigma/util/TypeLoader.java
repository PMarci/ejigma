package ejigma.util;

import ejigma.exception.TypeLoaderError;
import ejigma.model.type.*;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeLoader {

    private static final JAXBContext jaxbContext = initJaxbContext();

    private static final String PATH = getPath();

    private TypeLoader() {
        // hiding
    }

    public static List<RotorType> loadCustomRotorTypes() {
        List<RotorType> rotorTypes = Collections.emptyList();
        try {
            rotorTypes = getCustomRotorTypes();
        } catch (JAXBException | URISyntaxException | IOException e) {
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

    private static List<RotorType> getCustomRotorTypes() throws JAXBException, IOException, URISyntaxException {
        List<RotorType> result;
        URI uri = new URI(PATH);

        if (uri.getScheme().equals("jar")) {
            result = getJarFiles(uri);
        } else {
            result = getFSFiles(uri);

        }
        return result;
    }

    private static List<RotorType> getFSFiles(URI uri) throws JAXBException, IOException {
        List<RotorType> result = new ArrayList<>();
        CustomRotorType customRotorType;
        List<Path> sourceFiles = Collections.emptyList();
        try (Stream<Path> paths = Files.walk(Paths.get(uri), 1)) {
            if (paths != null) {
                sourceFiles = paths
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .collect(Collectors.toList());
            }
        }
        for (Path sourcePath : sourceFiles) {
            Unmarshaller unmarshaller = getUnmarshaller();
            InputStreamReader reader =
                    new InputStreamReader(new FileInputStream(sourcePath.toFile()), StandardCharsets.UTF_8);
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

    private static List<RotorType> getJarFiles(URI uri) throws IOException, JAXBException {
        List<RotorType> result = new ArrayList<>();
        CustomRotorType customRotorType;
        List<Path> sourceFiles;
        Path myPath;
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            myPath = fileSystem.getPath(PATH.substring(PATH.indexOf('!') + 1));
            sourceFiles = Collections.emptyList();
            try (Stream<Path> paths = Files.walk(myPath, 1)) {
                if (paths != null) {
                    sourceFiles = paths
                            .filter(s -> s.getFileName().toString().endsWith(".json"))
                            .collect(Collectors.toList());
                }
                for (Path sourcePath : sourceFiles) {
                    Unmarshaller unmarshaller = getUnmarshaller();
                    StreamSource source;
                    try (InputStream ins = TypeLoader.class.getResourceAsStream(sourcePath.toString())) {
                        InputStreamReader reader = null;
                        if (ins != null) {
                            reader = new InputStreamReader(ins);
                        }
                        source = new StreamSource(reader);
                        JAXBElement<CustomRotorType> customRotorTypeElem =
                                unmarshaller.unmarshal(source, CustomRotorType.class);
                        if (customRotorTypeElem != null) {
                            customRotorType = customRotorTypeElem.getValue();
                            if (customRotorType != null) {
                                result.add(customRotorType);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String getPath() {
        Class<TypeLoader> typeLoaderClass = TypeLoader.class;
        URL result = typeLoaderClass.getResource("");
        Optional<URL> resourceOpt = Optional.ofNullable(result);
        result = resourceOpt
                .or(() -> Optional.ofNullable(
                        typeLoaderClass.getResource(
                                "/" + typeLoaderClass.getPackageName().replace(".", "/"))))
                .orElseThrow(() -> new TypeLoaderError("Couldn't init TypeLoader!"));
        return result.toExternalForm();
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
                                            );
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }
}
