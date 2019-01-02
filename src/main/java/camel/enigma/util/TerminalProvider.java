package camel.enigma.util;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class TerminalProvider {

    private static Terminal terminal = null;

//    @EventListener(ContextRefreshedEvent.class)
//    public void contextRefreshedEvent() {
//        System.out.println("sup");
//    }

    @Bean
    public static Terminal initTerminal() throws IOException {
        Terminal terminal;
        terminal = TerminalBuilder.builder()
            .system(true)
            .encoding(StandardCharsets.UTF_8)
            .nativeSignals(true)
            .jansi(true)
            .build();
        terminal.enterRawMode();
        terminal.puts(InfoCmp.Capability.keypad_xmit);
        TerminalProvider.terminal = terminal;
        return terminal;
    }
}
