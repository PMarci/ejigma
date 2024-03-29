package ejigma.util;

import ejigma.exception.TypeLoaderError;
import ejigma.model.component.Scrambler;
import ejigma.model.type.ScramblerType;
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

// TODO since it's all instance methods not, maybe try putting type arguments into class
// maybe create anonymous instances with static builder methods
// reuse later when reloading is in
public class TypeLoader {

    private final JAXBContext jaxbContext;

    private static final String STATIC_PATH = getStaticPath();
    public static final String ENTRYWHEEL_TYPES_FOLDER = "entrywheels";
    public static final String ROTOR_TYPES_FOLDER = "rotors";
    public static final String REFLECTOR_TYPES_FOLDER = "reflectors";
    public static final String PLUGBOARD_CONFIGS_FOLDER = "plugboardconfigs";

    public TypeLoader(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public <
            C extends T,
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> List<C> loadCustomScramblerTypes(
            Class<C> customScramblerTypeClass,
            String subFolder) {
        List<C> scramblerTypes = Collections.emptyList();
        try {
            scramblerTypes = getCustomScramblerTypes(customScramblerTypeClass, subFolder);
        } catch (JAXBException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return scramblerTypes;
    }

    private <
            C extends T,
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> List<C> getCustomScramblerTypes(
            Class<C> customScramblerTypeClass,
            String subFolder) throws JAXBException, IOException, URISyntaxException {

        List<C> result;
        URI uri = new URI(STATIC_PATH + subFolder);

        if (uri.getScheme().equals("jar")) {
            result = getJarScramblerTypes(customScramblerTypeClass, uri);
        } else {
            result = getFSScramblerTypes(customScramblerTypeClass, uri);

        }
        return result;
    }

    private <
            C extends T,
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> List<C> getFSScramblerTypes(
            Class<C> customScramblerTypeClass,
            URI uri) throws JAXBException, IOException {

        List<C> result = new ArrayList<>();
        C scramblerType;
        List<Path> sourceFiles = Collections.emptyList();
        Path subFolder = Paths.get(uri);
        if (!subFolder.toFile().exists()) {
            subFolder.toFile().mkdir();
        }
        try (Stream<Path> paths = Files.walk(subFolder, 1)) {
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
            JAXBElement<C> customRotorTypeElem =
                    unmarshaller.unmarshal(source, customScramblerTypeClass);
            if (customRotorTypeElem != null) {
                scramblerType = customRotorTypeElem.getValue();
                if (scramblerType != null) {
                    result.add(scramblerType);
                }
            }
        }
        return result;
    }


    private <
            C extends T,
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> List<C> getJarScramblerTypes(
            Class<C> customScramblerTypeClass,
            URI uri) throws IOException, JAXBException {

        List<C> result = new ArrayList<>();
        C scramblerType;
        List<Path> sourceFiles;
        Path myPath;
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            String uriExtPath = uri.toString();
            myPath = fileSystem.getPath(uriExtPath.substring(uriExtPath.indexOf('!') + 1));
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
                        source = new StreamSource(ins);
                        JAXBElement<C> customRotorTypeElem =
                                unmarshaller.unmarshal(source, customScramblerTypeClass);
                        if (customRotorTypeElem != null) {
                            scramblerType = customRotorTypeElem.getValue();
                            if (scramblerType != null) {
                                result.add(scramblerType);
                            }
                        }
                    }
                }
            } catch (NoSuchFileException e) {
                // ignored, msg
            }
        }
        return result;
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        return unmarshaller;
    }

    private static String getStaticPath() {
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

}
