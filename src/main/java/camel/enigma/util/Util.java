package camel.enigma.util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {

    private Util() {
        // static helper class
    }

    public static boolean containsChar(char[] array, char inputChar) {
        for (char c : array) {
            if (c == inputChar) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsChar(String string, char inputChar) {
        return string.indexOf(inputChar) != -1;
    }

    public static int countOccurrences(String string, char inputChar) {
        int result = 0;
        for (char c : string.toCharArray()) {
            if (c == inputChar) {
                result++;
            }
        }
        return result;
    }

    public static String fisherYatesShuffle(String alphabet) {
        Random random = ThreadLocalRandom.current();
        int[] alphaArray = alphabet.codePoints().toArray();
        for (int i = alphaArray.length - 1; i > 0; i--) {
            int rnd = random.nextInt(i + 1);
            int a = alphaArray[rnd];
            alphaArray[rnd] = alphaArray[i];
            alphaArray[i] = a;

        }
        return new String(alphaArray, 0, alphaArray.length);
    }

    public static String generate2Cycles(String alphabetString) {
        int alphabetLength = alphabetString.length();
        Random random = ThreadLocalRandom.current();
        List<Integer> unswappedIndexList = IntStream.range(0, alphabetLength).boxed().collect(Collectors.toList());
        char[] outputArray = new char[alphabetLength];
        int rnd;
        int i = unswappedIndexList.size() - 1;
        while (i > 0) {
            rnd = random.nextInt(i);
            int a = unswappedIndexList.remove(i);
            int b = unswappedIndexList.remove(rnd);
            outputArray[b] = alphabetString.charAt(a);
            outputArray[a] = alphabetString.charAt(b);
            i -= 2;
        }
        // for odd numbers of letters
        if (!unswappedIndexList.isEmpty()) {
            int a = unswappedIndexList.remove(0);
            outputArray[a] = alphabetString.charAt(a);
        }
        return new String(outputArray, 0, outputArray.length);
    }
}
