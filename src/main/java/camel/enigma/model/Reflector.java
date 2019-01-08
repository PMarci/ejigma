package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.model.type.ReflectorType;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;

public class Reflector extends ScramblerWheel {

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
                String wiringString = Util.fisherYatesShuffle(alphabetString);
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
        };
    }



}
