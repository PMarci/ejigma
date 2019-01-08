package camel.enigma.util;

import camel.enigma.model.CustomRotorType;
import camel.enigma.model.type.RotorType;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class TypeLoader {

    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(CustomRotorType.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static String path = getPath();

    private static String getPath() {
        Class<TypeLoader> typeLoaderClass = TypeLoader.class;
        URL resource = typeLoaderClass.getResource("./");
        return resource.getPath();
    }

    @Bean
    public static List<RotorType> loadRotorTypes() {
        List<RotorType> rotorTypes = Collections.emptyList();
        List<File> sourceFiles = listFiles();
        try {
            rotorTypes = getCustomRotorTypes(sourceFiles);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return rotorTypes;
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

    private static List<RotorType> getCustomRotorTypes(List<File> sourceFiles) throws JAXBException {
        List<RotorType> result = new ArrayList<>();
        CustomRotorType customRotorType;
        for (File sourceFile : sourceFiles) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
            JAXBElement<CustomRotorType> customRotorTypeElem = unmarshaller.unmarshal(new StreamSource(sourceFile), CustomRotorType.class);
            if (customRotorTypeElem != null) {
                customRotorType = customRotorTypeElem.getValue();
                if (customRotorType != null) {
                    result.add(customRotorType);
                }
            }
        }
        return result;
    }
}
