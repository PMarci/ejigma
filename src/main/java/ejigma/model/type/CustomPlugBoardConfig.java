package ejigma.model.type;


import ejigma.model.PlugBoard;
import ejigma.model.Scrambler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "customPlugBoardConfig")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CustomPlugBoardConfig implements CustomScramblerType<PlugBoard>, PlugBoardConfig, Serializable {

    private String name = "DEFAULT";
    private String alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
    private String initString = "";
    private String sourceString = "";
    private String wiringString = "";

    public CustomPlugBoardConfig() {
        // jaxb
    }

    public CustomPlugBoardConfig(String name, String alphabetString, String sourceString, String wiringString) {
        this.name = name;
        this.alphabetString = alphabetString;
        this.sourceString = sourceString;
        this.wiringString = wiringString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getInitString() {
        return initString;
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
