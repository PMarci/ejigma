package ejigma;

import ejigma.exception.ArmatureInitException;
import ejigma.exception.ScramblerSettingException;
import ejigma.model.Armature;
import ejigma.model.Enigma;
import ejigma.model.PlugBoard;
import ejigma.model.type.*;
import ejigma.util.ScrambleResult;
import org.jline.terminal.Terminal;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) {
        try {
            ConfigContainer configContainer = new ConfigContainer();
            Enigma enigma = new Enigma();
            Map<Character, List<String>> opts = getOpts(args);
            // Interactive mode
            if (!opts.containsKey('-') && !opts.containsKey('f')) {
                startInteractive(configContainer, enigma, opts);
                // Non-interactive mode
            } else startNonInteractive(configContainer, enigma, opts);
        } catch (IOException | ArmatureInitException | ScramblerSettingException e) {
            e.printStackTrace();
        }
    }

    private static void startInteractive(ConfigContainer configContainer,
                                  Enigma enigma,
                                  Map<Character, List<String>> opts) throws ArmatureInitException, ScramblerSettingException, IOException {
        enigma.init(configContainer);
        if (opts.containsKey('p')) {
            enigma.setPlugBoard(new PlugBoard());
        }
        printGreeting(enigma.getTerminal(), configContainer);
        anyKey(enigma.getTerminal());
        enigma.start();
    }

    private static void startNonInteractive(ConfigContainer configContainer,
                                            Enigma enigma,
                                            Map<Character, List<String>> opts) throws ArmatureInitException, IOException {

        RotorType[] rotorTypes = Armature.DEFAULT_ROTOR_TYPES;
        EntryWheelType entryWheelType = Armature.DEFAULT_ENTRY_WHEEL_TYPE;
        ReflectorType reflectorType = Armature.DEFAULT_REFLECTOR_TYPE;
        if (opts.containsKey('r')) {
            List<RotorType> list = new ArrayList<>();
            for (String s : opts.get('r')) {
                RotorType type = getScramblerFromOpt(configContainer,
                                                     s.substring(2),
                                                     ConfigContainer::getRotorTypes,
                                                     "RotorType");
                list.add(type);
            }
            rotorTypes = list.toArray(new RotorType[0]);
        }
        if (opts.containsKey('e')) {
            entryWheelType = getScramblerFromOpt(configContainer,
                                                 opts.get('e').get(0).substring(2),
                                                 ConfigContainer::getEntryWheelTypes,
                                                 "EntryWheelType");
        }
        if (opts.containsKey('l')) {
            reflectorType = getScramblerFromOpt(configContainer,
                                                opts.get('l').get(0).substring(2),
                                                ConfigContainer::getReflectorTypes,
                                                "ReflectorType");
        }
        enigma.init(configContainer, entryWheelType, rotorTypes, reflectorType);
        if (opts.containsKey('\u0000')) {
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String output = enigma.scramble(sb.toString());
            enigma.getTerminal().writer().write(output);
            enigma.getTerminal().flush();
        }
        if (opts.containsKey('f')) {
            enigma.getTerminal().writer().flush();
            String input = readAll(opts.get('f').get(0).substring(2), enigma.getTerminal());
            String output = enigma.scramble(input);
            enigma.getTerminal().writer().write(output + "\n");
            enigma.getTerminal().flush();
        }
    }

    private static Map<Character, List<String>> getOpts(String[] args) {
        return Arrays.stream(args)
                .filter(s -> s.length() > 0 && s.charAt(0) == '-')
                .collect(Collectors.groupingBy(s -> {
                    if (s.length() > 1) {
                        return s.charAt(1);
                    } else {
                        return '\u0000';
                    }
                }));
    }

    @SuppressWarnings("unchecked")
    private static <T extends ScramblerType<?>> T getScramblerFromOpt(ConfigContainer configContainer,
                                                                      String opt,
                                                                      Function<ConfigContainer, List<T>> get,
                                                                      String type) throws ArmatureInitException {

        EntryWheelType entryWheelType;
        entryWheelType = (EntryWheelType) get.apply(configContainer).stream()
                .filter(entryWheelType1 -> entryWheelType1.getName().equals(opt))
                .findAny()
                .orElseThrow(() -> new ArmatureInitException(
                        String.format(
                                "Couldn't find an %s for param %s",
                                type,
                                opt)));
        return (T) entryWheelType;
    }

    private static void printGreeting(Terminal terminal, ConfigContainer configContainer) throws IOException {
        ScrambleResult.HistoryEntry.printBanner(terminal.writer());
        terminal.writer().write(
                String.format(
                        "Welcome to the camel-enigma cli, your terminal type is: %s%n",
                        terminal.getClass().getSimpleName()) +
                        String.format(
                                "The following rotor types are available: %n%s%n",
                                configContainer.getRotorTypes().toString()) +
                        String.format(
                                "The following reflector types are available: %n%s%n",
                                configContainer.getReflectorTypes().toString()) +
                        String.format(
                                "The following entry wheel types are available: %n%s%n",
                                configContainer.getEntryWheelTypes().toString()));
    }

    private static void anyKey(Terminal terminal) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        terminal.writer().write("Press any key to continue");
        try {
            input.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readAll(String filename, Terminal terminal) {
        StringBuilder sb = new StringBuilder();
        Path path = Path.of(filename);
        File file = path.toFile();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            terminal.writer().write(e.getMessage());
            terminal.flush();
        }
        return sb.toString();
    }
}
