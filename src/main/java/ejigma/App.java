package ejigma;

import ejigma.exception.ArmatureInitException;
import ejigma.io.KeyBoard;
import ejigma.io.LightBoard;
import ejigma.model.Armature;
import ejigma.model.type.ConfigContainer;
import ejigma.util.ScrambleResult;
import ejigma.util.TerminalProvider;
import org.jline.terminal.Terminal;

import java.io.*;
import java.nio.file.Path;

public class App {

    public static void main(String[] args) {
        try {
            Terminal terminal = TerminalProvider.initTerminal();
            ConfigContainer configContainer = new ConfigContainer();
            Armature armature = new Armature();
            LightBoard lightBoard = new LightBoard(terminal, armature);
            KeyBoard keyBoard = new KeyBoard(terminal, configContainer, armature, lightBoard);
            if (args.length == 0) {
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
                anyKey(terminal);
                terminal.flush();
                lightBoard.display();
                keyBoard.doStart();
            } else if (args.length == 1) {
                String input = readAll(args[0], terminal);
                String output = armature.scramble(input);
                terminal.writer().write(output + "\n");
                terminal.flush();
            }
        } catch (IOException | ArmatureInitException e) {
            e.printStackTrace();
        }
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
