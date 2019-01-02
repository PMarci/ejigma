package camel.enigma;

import camel.enigma.io.KeyBoardEndpoint;
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

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("wayhoo");
        Terminal terminal = camelContext.getEndpoint("keyboard", KeyBoardEndpoint.class).getTerminal();
//        Terminal terminal = null;
//        try {
//            terminal = TerminalBuilder.builder()
//                .system(true)
//                .encoding(StandardCharsets.UTF_8)
//                .nativeSignals(true)
//                //            .signalHandler(signal -> {
//                //                if (signal == Terminal.Signal.INT) {
//                //                    terminal.pause();
//                //                    try {
//                //                        exitPrompt();
//                //                    } catch (IOException e) {
//                //                        e.printStackTrace();
//                //                    }
//                //                }
//                //            })
//                .jansi(true)
//                .build();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
        System.out.println(terminal.toString());
//        Attributes prevAttr = terminal.enterRawMode();
//        System.out.println(terminal.getCursorPosition(null).toString());
//        terminal.setAttributes(prevAttr);
    }
}
