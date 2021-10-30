package ejigma.model.type.custom;


import ejigma.model.component.PlugBoard;
import ejigma.model.type.CustomScramblerType;
import ejigma.model.type.PlugBoardConfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@SuppressWarnings("unused")
@XmlRootElement(name = "customPlugBoardConfig")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CustomPlugBoardConfig extends PlugBoardConfig implements CustomScramblerType<PlugBoard, PlugBoardConfig>, Serializable {

    private String name = "DEFAULT";

    public CustomPlugBoardConfig() {
        // jaxb
    }

    public CustomPlugBoardConfig(String name, String alphabetString, String sourceString, String wiringString) {
        super(sourceString, wiringString, alphabetString);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInitString(String initString) {
        this.initString = initString;
    }

    public void setAlphabetString(String alphabetString) {
        this.alphabetString = alphabetString;
    }

    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }

    public void setWiringString(String wiringString) {
        this.wiringString = wiringString;
    }
}
