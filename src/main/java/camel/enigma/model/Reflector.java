package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;

class Reflector extends Scrambler {

    private ReflectorType reflectorType;

    public Reflector() throws ScramblerSettingException {
        super();
    }

    public Reflector(ReflectorType reflectorType) throws ScramblerSettingException {
        super();
        this.reflectorType = reflectorType;
    }

    public Reflector(String wiringString) throws ScramblerSettingException {
        super(wiringString);
    }

    public Reflector(String wiringString, ReflectorType reflectorType) throws ScramblerSettingException {
        super(wiringString);
        this.reflectorType = reflectorType;
    }

    public Reflector(String alphabetString, String wiringString) throws ScramblerSettingException {
        super(alphabetString, wiringString);
    }

    public Reflector(String alphabetString, String wiringString, ReflectorType reflectorType) throws ScramblerSettingException {
        super(alphabetString, wiringString);
        this.reflectorType = reflectorType;
    }

    @Override
    ScrambleResult scramble(ScrambleResult input) {
        char key = input.getResult();
        char value = get(key);
        // TODO better default
        input.putResult(value, reflectorType.name(),key, value,0, '@');
        return input;
    }

    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        return scramble(input);
    }

    public ReflectorType getReflectorType() {
        return reflectorType;
    }

    public void setReflectorType(ReflectorType reflectorType) {
        this.reflectorType = reflectorType;
    }
}
