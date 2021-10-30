package ejigma.model.type.custom;


import ejigma.model.PlugBoard;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setInitString(String initString) {
        this.initString = initString;
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    public void setAlphabetString(String alphabetString) {
        this.alphabetString = alphabetString;
    }

    public String getSourceString() {
        return sourceString;
    }

    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }

    public String getWiringString() {
        return wiringString;
    }

    public void setWiringString(String wiringString) {
        this.wiringString = wiringString;
    }
}
