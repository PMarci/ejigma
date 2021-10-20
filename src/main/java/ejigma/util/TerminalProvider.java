package ejigma.util;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TerminalProvider {

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
        return terminal;
    }
}
