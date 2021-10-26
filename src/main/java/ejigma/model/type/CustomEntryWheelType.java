package ejigma.model.type;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.EntryWheel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "customEntryWheelType")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CustomEntryWheelType implements CustomScramblerType<EntryWheel, EntryWheelType>, EntryWheelType, Serializable {

    private String name;
    private String alphabetString;
    private String wiringString;

    public CustomEntryWheelType() {
        // jaxb
    }

    public CustomEntryWheelType(String name, String alphabetString, String wiringString) {
        this.name = name;
        this.alphabetString = alphabetString;
        this.wiringString = wiringString;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EntryWheel freshScrambler() {
        EntryWheel result = null;
        try {
            result = new EntryWheel(alphabetString, wiringString, this);
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
