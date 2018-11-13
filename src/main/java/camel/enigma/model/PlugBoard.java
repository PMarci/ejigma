package camel.enigma.model;

import camel.enigma.exception.ScramblerSettingException;
import camel.enigma.util.ScrambleResult;

public class PlugBoard extends Scrambler {

    private String sourceString;

    PlugBoard(String alphabetString, String sourceString, String wiringString, ScramblerType scramblerType)
        throws ScramblerSettingException {
        super(alphabetString, wiringString, scramblerType);
        this.sourceString = sourceString;
        // TODO validate shorter than alphabet, contains only alphabet (or filter)
        initWiring();
    }

    @Override
    void validateWiringString(String wiringString) throws ScramblerSettingException {
        // TODO
    }

    @Override
    void initWiring() {
        setWiring(sourceString, wiringString);
    }

    @Override
    void setWiring(String sourceString, String wiringString) {
        char[] alphabetArray = alphabetString.toCharArray();
        this.forwardLinks = new int[alphabetArray.length];
        for (int i = 0; i < alphabetArray.length; i++) {
            forwardLinks[i] = -1;
        }
        char[] sourceArray = sourceString.toCharArray();
        char[] wiringArray = wiringString.toCharArray();
        int i = 0;
        int j = 0;
        boolean lastAlpha;
        char wiringChar;
        char sourceChar;
        int outputAddress;
        char alphabetChar;
        boolean match = false;
        boolean unmapped;
        boolean lastSource;
        int lastSourceIndex = sourceArray.length - 1;
        int lastAlphaIndex = alphabetArray.length - 1;
        do {
            sourceChar = sourceArray[i];
            wiringChar = wiringArray[i];
            outputAddress = alphabetString.indexOf(wiringChar);
            do {
                unmapped = forwardLinks[j] == j || forwardLinks[j] == -1;
                if (unmapped) {
                    alphabetChar = alphabetArray[j];
                    match = sourceChar == alphabetChar;
                    forwardLinks[j] = match ? outputAddress : j;
                }
                lastAlpha = j >= lastAlphaIndex;
                lastSource = i >= lastSourceIndex;
                j = (!lastAlpha) ? j + 1 : 0;
            } while (!match && !(lastSource && lastAlpha));
            i = (!lastSource) ? i + 1 : lastSourceIndex;
        } while (!(lastSource && lastAlpha));
        this.reverseLinks = this.forwardLinks;
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
    ScrambleResult scramble(ScrambleResult input) {
        return scrambleInput(input, forwardLinks);
    }

    @Override
    ScrambleResult reverseScramble(ScrambleResult input) {
        return scramble(input);
    }
}
