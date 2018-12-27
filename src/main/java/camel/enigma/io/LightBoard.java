package camel.enigma.io;

import camel.enigma.model.Armature;
import camel.enigma.util.SettingManager;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.InvalidPayloadException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.Cursor;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;
import org.jline.utils.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.fusesource.jansi.Ansi.ansi;

@Component
public class LightBoard {

    private static final Logger logger = LoggerFactory.getLogger(LightBoard.class);

    @Autowired
    private Armature armature;

    private static Terminal terminal;
    private static Status status;
    private StringBuilder stringBuilder;
    private int lineNo;
    private int oldLineNo;
    private int len;
    private static Cursor pos;

    public LightBoard() {
        clearBuffer();
        AnsiConsole.systemInstall();
        stringBuilder = new StringBuilder();
    }

    @Handler
    public void handle(Exchange exchange) {
        try {
            String s = exchange.getIn().getMandatoryBody(String.class);
            if (SettingManager.isDetailMode()) {
                terminal.puts(InfoCmp.Capability.cursor_home);
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.writer().write(s);
            } else {
                stringBuilder.append(s);

                int terminalWidth = terminal.getWidth();
                int terminalHeight = terminal.getHeight();
                oldLineNo = lineNo;
                int sbLength = stringBuilder.length();
                lineNo = (sbLength - 1) / terminalWidth;
                len = (sbLength - 1) % terminalWidth + 1;
                advanceLine(lineNo, oldLineNo);
                terminal.puts(InfoCmp.Capability.carriage_return);
                terminal.puts(InfoCmp.Capability.clr_eol);
                for (int i = 0; i < lineNo; i++) {
                    terminal.puts(InfoCmp.Capability.cursor_up);
                    terminal.puts(InfoCmp.Capability.clr_eol);
                }
                terminal.writer().write(stringBuilder.toString());
                List<AttributedString> statusStrings = createStatusStrings();
                updateStatus(statusStrings);
            }
        } catch (InvalidPayloadException e) {
            logger.error("Invalid payload!", e);
        }
    }

    public List<AttributedString> createStatusStrings() {
        List<AttributedString> statusStrings = armature.getOffsetString().chars()
                .mapToObj(value -> AttributedString.fromAnsi(
                        ansi()
                                .bgBright(Ansi.Color.BLACK)
                                .fg(Ansi.Color.WHITE)
                                .render(String.valueOf(((char) value)))
                                .reset()
                                .toString()))
                .collect(Collectors.toList());
        terminal.flush();
        updateCursorPos();
        asList("lineNo: " + lineNo,
               ", oldLineNo: " + oldLineNo,
               ", prevPos: " + pos.toString(),
//               ", newLine: " + (oldLineNo < lineNo))
                ", len: " + len)
                .forEach(s1 -> statusStrings.add(AttributedString.fromAnsi(s1)));
        return statusStrings;
    }

    private static void updateCursorPos() {
        pos = terminal.getCursorPosition(null);
    }

    private static void advanceLine(int lineNo, int oldlineNo) {
        // no idea why this works this way
        int diff = lineNo - oldlineNo;
        advanceForDiff(diff);
    }

    public static void advanceForDiff(int diff) {
        if (diff > 0) {
            terminal.puts(InfoCmp.Capability.scroll_forward);
            for (int i = 0; i < diff; i++) {
                terminal.puts(InfoCmp.Capability.scroll_forward);
                terminal.puts(InfoCmp.Capability.cursor_up);
            }
            if (diff >= 2) {
                terminal.puts(InfoCmp.Capability.cursor_up);
            }
        }
    }

    public void updateStatus(List<AttributedString> statusStrings) {
        status.resize();
        terminal.puts(InfoCmp.Capability.scroll_forward);
        status.update(Collections.singletonList(AttributedString.join(new AttributedString(" "), statusStrings)));
        terminal.puts(InfoCmp.Capability.cursor_up);
        terminal.puts(InfoCmp.Capability.column_address, pos.getX());
    }

    static void setTerminal(Terminal terminal) {
        LightBoard.terminal = terminal;
        status = Status.getStatus(terminal);
        status.resize();
    }

    public void clearBuffer() {
        stringBuilder = new StringBuilder();
        lineNo = 0;
        oldLineNo = 0;
    }
}
