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
    private List<String> oldLines = Collections.emptyList();

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
    }

    public void display(List<String> toDisplay) {
        display.clear();
        display.reset();
        int columns;
        int defaultColumns = 80;
        int targetColumns = (columns = size.getColumns()) > 0 ? columns : defaultColumns;
        display.resize(size.getRows(), targetColumns);
        int cursorpos;
        int targetCursorPos = (cursorpos = buf.cursor()/*size.cursorPos(0, 0)*/) > -1 ? cursorpos : 0;
        display.updateAnsi(toDisplay, targetCursorPos);
        oldLines = toDisplay;
    }

    public void redisplay() {
        display(oldLines);
    }

    public void clearBuffer() {
        buf.clear();
        lineNo = 0;
        oldLineNo = 0;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public Buffer getBuffer() {
        return buf;
    }
}
