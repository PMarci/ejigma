package camel.enigma.io;

import camel.enigma.util.SettingManager;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.jline.reader.Buffer;
import org.jline.reader.LineReader;
import org.jline.reader.impl.BufferImpl;
import org.jline.terminal.Cursor;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;
import org.jline.utils.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class LightBoard extends DefaultProducer {

    private static final Logger logger = LoggerFactory.getLogger(LightBoard.class);

    private KeyBoardEndpoint endpoint;
    private Terminal terminal;
    private Status status;
    private Size size;
    private final Buffer buf;
    private Display display;
    private int lineNo;
    private int oldLineNo;
    private int len;
    private Cursor pos;
    private LineReader selectRotorReader;
    private List<String> oldLines;

    public LightBoard(KeyBoardEndpoint endpoint, Terminal terminal) {
        super(endpoint);
        this.endpoint = endpoint;
        this.terminal = terminal;
        size = new Size();
        size.copy(terminal.getSize());
        display = new Display(terminal, false);
        // not setting this to true worsens linebreak behavior (tested in CMD thus far)
        display.setDelayLineWrap(true);
        buf = new BufferImpl();
        clearBuffer();
    }

    @Override
    public void process(Exchange exchange) {

        size.copy(terminal.getSize());
        List<String> s = exchange.getIn().getBody(List.class);
        List<String> toDisplay;
        if (SettingManager.isDetailMode()) {
            terminal.puts(InfoCmp.Capability.clear_screen);
            toDisplay = s;
        } else {
            s.forEach(buf::write);
            toDisplay = Collections.singletonList(buf.toString());
//                int terminalWidth = terminal.getWidth();
//                oldLineNo = lineNo;
//                int sbLength = /*staringBuilder.length();*/buf.length();
//                lineNo = (sbLength - 1) / terminalWidth;
//                len = (sbLength - 1) % terminalWidth + 1;
//                advanceLine(lineNo, oldLineNo);
//                terminal.puts(InfoCmp.Capability.carriage_return);
//                terminal.puts(InfoCmp.Capability.clr_eol);
//                for (int i = 0; i < lineNo; i++) {
//                    terminal.puts(InfoCmp.Capability.cursor_up);
//                    terminal.puts(InfoCmp.Capability.clr_eol);
//                }
//                System.out.println(buf.toString());
//                terminal.writer().write(buf.toString());
//                terminal.flush();

//                display.resize(size.getRows(), size.getColumns());
//                updateCursorPos();
//                System.out.println(pos.toString());
//                List<AttributedString> statusStrings = createStatusStrings();
//                updateStatus(statusStrings);
        }
        // TODO cursor not reset one letter after newline in cmd
        // TODO also look into that gh issue about overriding the readline method
        display(toDisplay);
        oldLines = toDisplay;
    }

    public void display(List<String> toDisplay) {
        display.clear();
        display.reset();
        int columns;
        int defaultColumns = 80;
        int targetColumns = (columns = size.getColumns()) > 0 ? columns : defaultColumns;
        display.resize(size.getRows(), targetColumns);
        int cursorpos;
        int targetCursorPos = (cursorpos = size.cursorPos(0, 0)) > -1 ? cursorpos : 0;
        display.updateAnsi(toDisplay, targetCursorPos);
    }

    public void clearBuffer() {
        buf.clear();
        lineNo = 0;
        oldLineNo = 0;
    }

//    private List<AttributedString> stringListToAttrStringList(List<String> toDisplay) {
//        return toDisplay.stream().sequential()
//                .collect(
//                        Collector.of(
//                                ArrayList::new,
//                                (attributedStrings, s1) -> attributedStrings.add(AttributedString.fromAnsi(s1)),
//                                (list, list2) -> {
//                                    list.addAll(list2);
//                                    return list;
//                                }
//                                    ));
//    }

//    public List<AttributedString> createStatusStrings() {
//        List<AttributedString> statusStrings = armature.getOffsetString().chars()
//                .mapToObj(value -> AttributedString.fromAnsi(
//                        ansi()
//                                .bgBright(Ansi.Color.BLACK)
//                                .fg(Ansi.Color.WHITE)
//                                .render(String.valueOf(((char) value)))
//                                .reset()
//                                .toString()))
//                .collect(Collectors.toList());
//        terminal.flush();
//        asList("lineNo: " + lineNo,
//               ", oldLineNo: " + oldLineNo,
////               ", prevPos: " + pos.toString(),
////               ", newLine: " + (oldLineNo < lineNo))
//               ", len: " + len)
//                .forEach(s1 -> statusStrings.add(AttributedString.fromAnsi(s1)));
//        return statusStrings;
//    }

//    private void updateCursorPos() {
//        pos = terminal.getCursorPosition(null);
//    }

//    private void advanceLine(int lineNo, int oldlineNo) {
//        int diff = lineNo - oldlineNo;
//        advanceForDiff(diff);
//    }

//    private void advanceForDiff(int diff) {
//        // no idea why this works this way
//        if (diff > 0) {
//            terminal.puts(InfoCmp.Capability.scroll_forward);
//            for (int i = 0; i < diff; i++) {
//                terminal.puts(InfoCmp.Capability.scroll_forward);
//                terminal.puts(InfoCmp.Capability.cursor_up);
//            }
//            if (diff >= 2) {
//                terminal.puts(InfoCmp.Capability.cursor_up);
//            }
//        }
//    }

//    public void updateStatus(List<AttributedString> statusStrings) {
//        status.resize();
//        terminal.puts(InfoCmp.Capability.scroll_forward);
//        status.update(Collections.singletonList(AttributedString.join(new AttributedString(" "), statusStrings)));
//        terminal.puts(InfoCmp.Capability.cursor_up);
//        terminal.puts(InfoCmp.Capability.column_address, 0/*pos.getX()*/);
//    }
//
//    private void updateStatus(String statusString) {
//        status.resize();
//        status.update(Collections.singletonList(AttributedString.fromAnsi(statusString)));
//        terminal.puts(InfoCmp.Capability.column_address, 0/*pos.getX()*/);
//    }

//    private void initTerm() throws IOException {
//        terminal = TerminalBuilder.builder()
//            .system(true)
//            .encoding(StandardCharsets.UTF_8)
//            .nativeSignals(true)
//            .signalHandler(signal -> {
//                if (signal == Terminal.Signal.INT) {
//                    terminal.pause();
//                    try {
//                        exitPrompt();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            })
//            .jansi(true)
//            .build();
//        terminal.enterRawMode();
////        this.status = Status.getStatus(terminal);
////        status.resize();
//    }

//    private void initSecondTerm() throws IOException {
//        terminal = TerminalBuilder.builder()
//                .system(true)
//                .encoding(StandardCharsets.UTF_8)
//                .signalHandler(Terminal.SignalHandler.SIG_IGN)
//                .jansi(true)
//                .build();
//        terminal.enterRawMode();
////        this.status = Status.getStatus(terminal);
////        status.resize();
//    }
//
//    public Terminal getTerminal() {
//        return terminal;
//    }
//
//    private void exitPrompt() throws IOException {
//        char input;
//        try {
//            terminal.reader().close();
//            terminal.close();
//            initSecondTerm();
//            // this interrupts the reader
//            terminal.reader().read(1);
//        } catch (InterruptedIOException ignored) {
//            // ignore
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        do {
////            updateStatus("Received SIGINT via Ctrl+C, exit application? [y/n]");
//            logger.info("Received SIGINT via Ctrl+C, exit application? [y/n]");
//            input = ((char) terminal.reader().read());
//        } while (input != 'y' && input != 'Y' && input != 'n' && input != 'N');
//        if (input == 'y' || input == 'Y') {
//            logger.info("\nExiting...");
//            try {
//                terminal.close();
////                camelContext.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.exit(0);
//        } else {
//            logger.info("Resuming...");
////            updateStatus("Resuming...");
//            try {
//                // the key to making a prompt like this work seems to be
//                // interrupting the waiting readers thread in the main loop
//                terminal.close();
////                initTerm();
////                endpoint.restartConsumer();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
