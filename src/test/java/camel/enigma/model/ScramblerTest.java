package camel.enigma.model;

import camel.enigma.util.ScrambleResult;
import org.apache.camel.util.IOHelper;
import org.fusesource.jansi.Ansi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

// TODO figure out if needed
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({"routeless", "test"})
public class ScramblerTest {

//    private Rotor errorRotor;

    @Value("${spring.output.ansi.enabled}")
    String springAnsiEnabled;

    @Before
    public void setUp() throws Exception {
//        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
//        errorRotor = new Rotor(incorrectWiringsString);
    }
    // TODO fix

    @Test
    public void testPrinting() throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        Writer writer = new OutputStreamWriter(System.out, charset);
        BufferedWriter bw = IOHelper.buffered(writer);
        System.out.printf("%nAnsi detected: %b%n", Ansi.isDetected());
        System.out.printf("Ansi enabled: %b%n", Ansi.isEnabled());
        System.out.printf("Spring ansi enabled: %s%n", springAnsiEnabled);
        String linesString;
        List<String> lines = ScrambleResult.HistoryEntry.loadLetter(4);
        List<String> lines2 = ScrambleResult.HistoryEntry.loadLetter(26);
        int linesSize = lines.size();
        int linesSize2 = lines2.size();
        for (int i = 0; i < linesSize || i < linesSize2; i++) {
            String line = (linesString = (i < linesSize) ? lines.get(i) : "") +
                    ScrambleResult.HistoryEntry.getPadding(linesString, 22) +
                    ((i < linesSize2) ? ansi().fg(Ansi.Color.RED).render(lines2.get(i)).reset().toString() : "");
            bw.write(line);
            bw.write(System.lineSeparator());
            bw.flush();
        }
        bw.write("╔══╦══╗");
        bw.write(System.lineSeparator());
        bw.flush();
        bw.write("╠═" + ansi().fgBright(Ansi.Color.BLACK).render("═").reset() + "║" + ansi().fgBright(Ansi.Color.BLACK).render("═").reset() + "═╣");
        bw.write(System.lineSeparator());
        bw.flush();
        bw.write("╚══╩══╝");
        bw.write(System.lineSeparator());
        bw.flush();
    }
}