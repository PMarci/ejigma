package camel.enigma.io;

import camel.enigma.util.SettingManager;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.util.IOHelper;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.fusesource.jansi.Ansi.ansi;

public class LightBoard {

    public LightBoard() {
        AnsiConsole.systemInstall();
    }

    @Handler
    public void handle(Exchange exchange) {
        if (SettingManager.isDetailMode()) {
            System.out.print(ansi().cursor(1, 1).toString() + "\u001B[3J" + "\u001B[2J");
        }
        try {
            String s = exchange.getIn().getMandatoryBody(String.class);
            Charset charset = StandardCharsets.UTF_8;
            Writer writer = new OutputStreamWriter(System.out, charset);
            BufferedWriter bw = IOHelper.buffered(writer);
            bw.write(s);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidPayloadException e) {
            e.printStackTrace();
        }
    }
}
