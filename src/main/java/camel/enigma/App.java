package camel.enigma;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.io.KeyBoard;
import camel.enigma.io.LightBoard;
import camel.enigma.model.Armature;
import camel.enigma.model.type.ConfigContainer;
import camel.enigma.util.TerminalProvider;
import org.jline.terminal.Terminal;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        Terminal terminal = null;
        try {
            terminal = TerminalProvider.initTerminal();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigContainer configContainer = new ConfigContainer();
        Armature armature = null;
        try {
            armature = new Armature();
        } catch (ArmatureInitException e) {
            e.printStackTrace();
        }
        LightBoard ligthBoard = new LightBoard(terminal, armature);
        KeyBoard keyBoard = new KeyBoard(terminal, configContainer, armature, ligthBoard);
        terminal.writer().write(
                String.format("Welcome to the camel-enigma cli, your terminal type is: %s%n", terminal.getClass()
                        .getSimpleName()));
        terminal.writer().write(
                String.format("The following rotor types are available: %n%s%n", configContainer.getRotorTypes().toString()));
        terminal.writer().write(
                String.format("The following reflector types are available: %n%s%n", configContainer.getReflectorTypes().toString()));
        terminal.writer().write(
                String.format("The following entry wheel types are available: %n%s%n", configContainer.getEntryWheelTypes().toString()));
        terminal.flush();
        keyBoard.doStart();
    }
}
