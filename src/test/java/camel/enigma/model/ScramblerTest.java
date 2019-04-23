package camel.enigma.model;

import camel.enigma.util.ScrambleResult;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application.properties")
@ActiveProfiles("test")
public class ScramblerTest {

//    private Rotor errorRotor;

    @Value("${spring.output.ansi.enabled}")
    private String springAnsiEnabled;

    @Before
    public void setUp() throws Exception {
//        String incorrectWiringsString = "A@CDEFGHIJKLMNOPQRSTUVWXYZ";
//        errorRotor = new Rotor(incorrectWiringsString);
    }
    // TODO fix

    @Test
    public void testPrinting() throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out, charset));
        System.out.printf("%nAnsi detected: %b%n", Ansi.isDetected());
        System.out.printf("Ansi enabled: %b%n", Ansi.isEnabled());
        System.out.printf("Spring ansi enabled: %s%n", springAnsiEnabled);
        String linesString;
        List<String> lines = ScrambleResult.HistoryEntry.loadLetter(4, "letters.txt");
        List<String> lines2 = ScrambleResult.HistoryEntry.loadLetter(26, "letters.txt");
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
        bw.write("╠═" + ansi().fgBright(Ansi.Color.BLACK).render("═").reset() + "║" +
                         ansi().fgBright(Ansi.Color.BLACK).render("═").reset() + "═╣");
        bw.write(System.lineSeparator());
        bw.flush();
        bw.write("╚══╩══╝");
        bw.write(System.lineSeparator());
        bw.flush();
    }
}