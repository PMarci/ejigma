package camel.enigma;

import camel.enigma.io.KeyBoardEndpoint;
import camel.enigma.model.Armature;
import camel.enigma.model.type.ConfigContainer;
import org.apache.camel.CamelContext;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ConfigContainer configContainer;

    @Autowired
    private Armature armature;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Terminal terminal = camelContext.getEndpoint("keyboard", KeyBoardEndpoint.class).getTerminal();
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
    }
}
