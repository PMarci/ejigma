package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.type.RotorType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "customRotorType")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CustomRotorType implements RotorType, Serializable {

    private String name;
    private String alphabetString;
    private String wiringString;
    private char[] notch;
    private boolean staticc = false;

    public CustomRotorType() {
        // jaxb
    }

    public CustomRotorType(
        String name,
        String alphabetString,
        String wiringString,
        char[] notch,
        boolean staticc) {

        this.name = name;
        this.alphabetString = alphabetString;
        this.wiringString = wiringString;
        this.notch = notch;
        this.staticc = staticc;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Rotor freshScrambler() {
        Rotor result = null;
        try {
            result = new Rotor(alphabetString, wiringString, notch, staticc, this);
        } catch (ScramblerSettingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
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

    public char[] getNotch() {
        return notch;
    }

    public void setNotch(char[] notch) {
        this.notch = notch;
    }

    public boolean isStaticc() {
        return staticc;
    }

    public void setStaticc(boolean staticc) {
        this.staticc = staticc;
    }
}
