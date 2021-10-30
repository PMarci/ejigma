package ejigma.model.type.custom;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.Reflector;
import ejigma.model.type.CustomScramblerType;
import ejigma.model.type.ReflectorType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@SuppressWarnings("unused")
@XmlRootElement(name = "customReflectorType")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CustomReflectorType implements CustomScramblerType<Reflector, ReflectorType>, ReflectorType, Serializable {


    private String name;
    private String alphabetString;
    private String wiringString;

    public CustomReflectorType() {
        // jaxb
    }

    public CustomReflectorType(String name, String alphabetString, String wiringString) {
        this.name = name;
        this.alphabetString = alphabetString;
        this.wiringString = wiringString;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Reflector freshScrambler() {
        Reflector result = null;
        try {
            result = new Reflector(alphabetString, wiringString, this);
        } catch (ScramblerSettingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlphabetString(String alphabetString) {
        this.alphabetString = alphabetString;
    }

    public String getWiringString() {
        return wiringString;
    }

    public void setWiringString(String wiringString) {
        this.wiringString = wiringString;
    }
}
