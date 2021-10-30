package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.ReflectorType;
import ejigma.util.ScrambleResult;

public class Reflector extends ScramblerWheel<Reflector, ReflectorType> {

    public Reflector(String alphabetString, String wiringString, ReflectorType reflectorType) throws ScramblerSettingException {
        this(alphabetString, wiringString, true, reflectorType);
    }

    private Reflector(String alphabetString, String wiringString, boolean staticc, ReflectorType reflectorType) throws ScramblerSettingException {
        super(alphabetString, wiringString, staticc, reflectorType);
    }

    @Override
    protected ScrambleResult scrambleInput(ScrambleResult input, int[] links) {
        int inputPos = input.getResult();
        char wiringInput = alphabet[inputPos];
        int link = links[inputPos];
        char wiringOutput = alphabet[link];
        return input.putResult(link, wiringInput, wiringOutput, wiringOutput, type.getName());
    }


    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        return scramble(input);
    }

    @Override
    public boolean isNotchEngaged() {
        return false;
    }

}
