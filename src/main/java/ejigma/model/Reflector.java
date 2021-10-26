package ejigma.model;

import ejigma.exception.ScramblerSettingException;
import ejigma.model.type.ReflectorType;
import ejigma.util.ScrambleResult;
import ejigma.util.Util;

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

    public static ReflectorType auto(String alphabetString) {
        return new ReflectorType() {
            @Override
            public String getName() {
                return "AUTO_REFLECTOR";
            }

            @Override
            public Reflector freshScrambler() {
                Reflector reflector = null;
                String wiringString = Util.generate2Cycles(alphabetString);
                try {
                    reflector = new Reflector(alphabetString, wiringString, this);
                } catch (ScramblerSettingException e) {
                    e.printStackTrace();
                }
                return reflector;
            }

            @Override
            public String getAlphabetString() {
                return alphabetString;
            }

            @Override
            public String toString() {
                return getName();
            }
        };
    }



}
