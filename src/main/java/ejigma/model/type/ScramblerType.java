package ejigma.model.type;

import ejigma.model.Scrambler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ScramblerType<S extends Scrambler> {

    Pattern TYPE_SUFFIX_PATTERN = Pattern.compile("^(?:Custom|Historic)(.*?)(?:Type|Config)$");

    String getName();

    S freshScrambler();

    String getAlphabetString();

    String toString();

    // TODO maybe smarter, issue is we don't have instances where we need them
    static String getScramblerName(String scramblerTypeTypeName) {
        Matcher typeMatcher = TYPE_SUFFIX_PATTERN.matcher(scramblerTypeTypeName);
        return (typeMatcher.find()) ? typeMatcher.group(1) : "Scrambler";
    }
}
