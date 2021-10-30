package ejigma.model.type.auto;

import ejigma.model.type.PlugBoardConfig;
import ejigma.util.Util;

import java.util.Arrays;
import java.util.Random;

public class AutoPlugBoardConfig extends PlugBoardConfig {

    private static final Random RANDOM = new Random();
    private static final String NAME = "AUTO_PLUGBOARD";

    private AutoPlugBoardConfig(String sourceString, String wiringString, String alphabetString) {
        super(sourceString, wiringString, alphabetString);
    }

    public static AutoPlugBoardConfig create(String alphabetString) {
        String sourceString = getRandomSource(alphabetString);
        return new AutoPlugBoardConfig(sourceString,
                                       Util.generate2Cycles(sourceString),
                                       alphabetString);
    }

    private static String getRandomSource(String alphabetString) {
        int aLen = alphabetString.length();
        int noOfPairs = RANDOM.nextInt(aLen);
        int sourceIndex;
        boolean[] sourceDrawn = new boolean[aLen];
        Arrays.fill(sourceDrawn, false);
        StringBuilder sourceBuilder = new StringBuilder();

        if (aLen > 2) {
            for (int i = 0; i < noOfPairs; i++) {
                do {
                    sourceIndex = RANDOM.nextInt(aLen);
                } while (sourceDrawn[sourceIndex]);
                sourceDrawn[sourceIndex] = true;
                sourceBuilder.append(alphabetString.charAt(sourceIndex));
            }
        }
        return sourceBuilder.toString();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSourceString() {
        return sourceString;
    }

    @Override
    public String getWiringString() {
        return wiringString;
    }

    @Override
    public String getAlphabetString() {
        return alphabetString;
    }

    public String getInitString() {
        return sourceString + '\u0000' + wiringString;
    }

}
