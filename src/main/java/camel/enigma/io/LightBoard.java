package camel.enigma.io;

import camel.enigma.model.Armature;
import camel.enigma.util.SettingManager;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.InvalidPayloadException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.Buffer;
import org.jline.reader.impl.BufferImpl;
import org.jline.terminal.Cursor;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
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

    @Autowired
    private CamelContext camelContext;

    private Terminal terminal;
    private Status status;
    private Size size;
    private final Buffer buf;
    private Display display;
    private int lineNo;
    private int oldLineNo;
    private int len;
    private Cursor pos;

    public LightBoard() {
        AnsiConsole.systemInstall();
//        stringBuilder = new StringBuilder();
        try {
            initTerm();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buf = new BufferImpl();
        clearBuffer();
    }

    @Handler
    public void handle(Exchange exchange) {
        try {
            //    private StringBuilder stringBuilder;
            AttributedStringBuilder attrStringBuilder = new AttributedStringBuilder();
        size = new Size(terminal.getWidth(), terminal.getHeight());
        display = new Display(terminal, false);
//            size.setColumns(terminal.getWidth());
//            size.setRows(terminal.getHeight());
            String s = exchange.getIn().getMandatoryBody(String.class);
            if (SettingManager.isDetailMode()) {
                terminal.puts(InfoCmp.Capability.cursor_home);
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.writer().write(s);
            } else {
                buf.write(s);
                AttributedString lines = attrStringBuilder.append(buf.toString()).toAttributedString();
                int terminalWidth = terminal.getWidth();
                oldLineNo = lineNo;
                int sbLength = /*stringBuilder.length();*/buf.length();
                lineNo = (sbLength - 1) / terminalWidth;
                len = (sbLength - 1) % terminalWidth + 1;
                advanceLine(lineNo, oldLineNo);
                terminal.puts(InfoCmp.Capability.carriage_return);
                terminal.puts(InfoCmp.Capability.clr_eol);
                for (int i = 0; i < lineNo; i++) {
                    terminal.puts(InfoCmp.Capability.cursor_up);
                    terminal.puts(InfoCmp.Capability.clr_eol);
                }
//                terminal.writer().write(buf.);

                display.resize(size.getRows(), size.getColumns());
                updateCursorPos();
                display.update(Collections.singletonList(lines), size.cursorPos(lineNo, pos.getX()));
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
        asList("lineNo: " + lineNo,
               ", oldLineNo: " + oldLineNo,
               ", prevPos: " + pos.toString(),
//               ", newLine: " + (oldLineNo < lineNo))
                ", len: " + len)
                .forEach(s1 -> statusStrings.add(AttributedString.fromAnsi(s1)));
        return statusStrings;
    }

    private void updateCursorPos() {
        pos = terminal.getCursorPosition(null);
    }

    private void advanceLine(int lineNo, int oldlineNo) {
        int diff = lineNo - oldlineNo;
        advanceForDiff(diff);
    }

    private void advanceForDiff(int diff) {
        // no idea why this works this way
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

    void updateStatus(String statusString) {
        status.resize();
        status.update(Collections.singletonList(AttributedString.fromAnsi(statusString)));
        terminal.puts(InfoCmp.Capability.column_address, pos.getX());
    }

    public void clearBuffer() {
//        stringBuilder = new StringBuilder();
        buf.clear();
        lineNo = 0;
        oldLineNo = 0;
    }

    private void initTerm() throws IOException {
        terminal = TerminalBuilder.builder()
                .system(true)
                .encoding(StandardCharsets.UTF_8)
                .nativeSignals(true)
                .signalHandler(signal -> {
                    if (signal == Terminal.Signal.INT) {
                        terminal.pause();
                        try {
                            exitPrompt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .jansi(true)
                .build();
        terminal.enterRawMode();
        this.status = Status.getStatus(terminal);
        status.resize();
    }

    private void initSecondTerm() throws IOException {
        terminal = TerminalBuilder.builder()
                .system(true)
                .encoding(StandardCharsets.UTF_8)
                .signalHandler(Terminal.SignalHandler.SIG_IGN)
                .jansi(true)
                .build();
        terminal.enterRawMode();
        this.status = Status.getStatus(terminal);
        status.resize();
    }

    public Terminal getTerminal() {
        return terminal;
    }

    private void exitPrompt() throws IOException {
        char input;
        try {
            terminal.reader().close();
            terminal.close();
            initSecondTerm();
            // this interrupts the reader
            terminal.reader().read(1);
        } catch (InterruptedIOException ignored) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        do {
            updateStatus("Received SIGINT via Ctrl+C, exit application? [y/n]");
            input = ((char) terminal.reader().read());
        } while (input != 'y' && input != 'Y' && input != 'n' && input != 'N');
        if (input == 'y' || input == 'Y') {
            logger.info("\nExiting...");
            try {
                terminal.close();
                camelContext.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            updateStatus("Resuming...");
            try {
                // the key to making a prompt like this work seems to be
                // interrupting the waiting readers thread in the main loop
                terminal.close();
                initTerm();
                // todo real solution
                camelContext.getEndpoint("keyboard", KeyBoardEndpoint.class).restartConsumer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
