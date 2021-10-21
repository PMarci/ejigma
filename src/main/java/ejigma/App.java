package ejigma;

import ejigma.exception.ArmatureInitException;
import ejigma.exception.ScramblerSettingException;
import ejigma.io.KeyBoard;
import ejigma.io.LightBoard;
import ejigma.model.Armature;
import ejigma.model.Enigma;
import ejigma.model.PlugBoard;
import ejigma.model.type.ConfigContainer;
import ejigma.model.type.EntryWheelType;
import ejigma.model.type.ReflectorType;
import ejigma.model.type.RotorType;
import ejigma.util.ScrambleResult;
import ejigma.util.TerminalProvider;
import org.jline.terminal.Terminal;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) {
        try {
            Enigma enigma = new Enigma();
            ConfigContainer configContainer = new ConfigContainer();
            enigma.setConfigContainer(configContainer);
            Terminal terminal;
            Armature armature;
            Map<Character, List<String>> opts = Arrays.stream(args)
                    .filter(s -> s.length() > 0 && s.charAt(0) == '-')
                    .collect(Collectors.groupingBy(s -> {
                        if (s.length() > 1) {
                            return s.charAt(1);
                        } else {
                            return '\u0000';
                        }
                    }));
            if (!opts.containsKey('-') && !opts.containsKey('f')) {
                terminal = TerminalProvider.initTerminal(false);
                enigma.setArmature(new Armature(enigma));
                enigma.setLightBoard(new LightBoard(terminal));
                enigma.setKeyBoard(new KeyBoard(terminal, enigma));
                if (opts.containsKey('p')) {
                    enigma.setPlugBoard(new PlugBoard());
                }
                printGreeting(terminal, configContainer);
                anyKey(terminal);
                enigma.start();
            } else {
                terminal = TerminalProvider.initTerminal(true);
                RotorType[] rotorTypes = Armature.DEFAULT_ROTOR_TYPES;
                EntryWheelType entryWheelType = Armature.DEFAULT_ENTRY_WHEEL_TYPE;
                ReflectorType reflectorType = Armature.DEFAULT_REFLECTOR_TYPE;
                if (opts.containsKey('r')) {
                    List<RotorType> list = new ArrayList<>();
                    for (String s : opts.get('r')) {
                        String substring = s.substring(2);
                        RotorType type = configContainer.getRotorTypes().stream()
                                .filter(rotorType -> rotorType.getName().equals(substring))
                                .findAny()
                                .orElseThrow(() -> new ArmatureInitException(
                                        String.format(
                                                "Couldn't find a RotorType for param %s",
                                                substring)));
                        list.add(type);
                    }
                    rotorTypes = list.toArray(new RotorType[0]);
                }
                if (opts.containsKey('e')) {
                    String substring = opts.get('e').get(0).substring(2);
                    entryWheelType = configContainer.getEntryWheelTypes().stream()
                            .filter(entryWheelType1 -> entryWheelType1.getName().equals(substring))
                            .findAny()
                            .orElseThrow(() -> new ArmatureInitException(
                                    String.format(
                                            "Couldn't find an EntryWheelType for param %s",
                                            substring)));
                }
                if (opts.containsKey('l')) {
                    String substring = opts.get('l').get(0).substring(2);
                    reflectorType = configContainer.getReflectorTypes().stream()
                            .filter(reflectorType1 -> reflectorType1.getName().equals(substring))
                            .findAny()
                            .orElseThrow(() -> new ArmatureInitException(
                                    String.format(
                                            "Couldn't find an ReflectorType for param %s",
                                            substring)));
                }
                armature = new Armature(entryWheelType, rotorTypes, reflectorType, enigma);
                enigma.setArmature(armature);
                if (opts.containsKey('\u0000')) {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    String output = enigma.scramble(sb.toString());
                    terminal.writer().write(output);
                    terminal.flush();
                }
                if (opts.containsKey('f')) {
                    terminal.writer().flush();
                    String input = readAll(opts.get('f').get(0).substring(2), terminal);
                    String output = enigma.scramble(input);
                    terminal.writer().write(output + "\n");
                    terminal.flush();
                }
            }
        } catch (IOException | ArmatureInitException | ScramblerSettingException e) {
            e.printStackTrace();
        }
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
