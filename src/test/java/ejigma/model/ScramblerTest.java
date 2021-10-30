package ejigma.model;

import ejigma.model.type.custom.CustomRotorType;
import ejigma.util.ScrambleResult;
import org.fusesource.jansi.Ansi;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.fusesource.jansi.Ansi.ansi;

public class ScramblerTest {

    private Rotor errorRotor;

    @Before
    public void setUp() throws Exception {
        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
        errorRotor = new CustomRotorType(
                "error",
                Scrambler.DEFAULT_ALPHABET_STRING,
                incorrectWiringsString,
                new char[]{'Q'},
                false).freshScrambler();
    }
    // TODO fix

    @Test
    public void testPrinting() throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out, charset));
        System.out.printf("%nAnsi detected: %b%n", Ansi.isDetected());
        System.out.printf("Ansi enabled: %b%n", Ansi.isEnabled());
        ScrambleResult.HistoryEntry.printBanner(bw);
        bw.write("╔══╦══╗");
        bw.write(System.lineSeparator());
        bw.flush();
        bw.write("╠═" + ansi().fgBright(Ansi.Color.BLACK).render("═").reset() + "║" +
                         ansi().fgBright(Ansi.Color.BLACK).render("═").reset() + "═╣");
        bw.write(System.lineSeparator());
        bw.flush();
        bw.write("╚══╩══╝");
        bw.write(System.lineSeparator());
        bw.flush();
    }

}