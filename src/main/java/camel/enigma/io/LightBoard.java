package camel.enigma.io;

import camel.enigma.util.ScrambleResult;
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
    private List<String> detailLines = Collections.emptyList();

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

        List<String> s;
        List<String> toDisplay;
        // TODO erase only oldlines amount also don't slam the screen
        terminal.puts(InfoCmp.Capability.clear_screen);
        if (SettingManager.isDetailMode()) {
            ScrambleResult scrambleResult = exchange.getIn().getBody(ScrambleResult.class);
            s = scrambleResult.printHistory();
            Character detailModeChar = scrambleResult.getResultAsChar();
            toDisplay = s;
            detailLines = s;
            buf.write(detailModeChar);
            toDisplay.add(buf.toString());
        } else {
            s = exchange.getIn().getBody(List.class);
            detailLines = Collections.emptyList();
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
        // TODO cursor not reset one letter after newline
        // TODO also look into that gh issue about overriding the readline method
        display(toDisplay);
    }

    public void display(List<String> toDisplay) {
        size.copy(terminal.getSize());
        display.clear();
        display.reset();
        int columns;
        int defaultColumns = 80;
        int targetColumns = (columns = size.getColumns()) > 0 ? columns : defaultColumns;
        display.resize(size.getRows(), targetColumns);
        int targetRow = (detailLines.size() - 1 >= 0) ? (detailLines.size() - 1) : 0;
        int cursorPos = buf.cursor() + size.cursorPos(targetRow, 0);
        int targetCursorPos = (cursorPos) > -1 ? cursorPos : 0;
        display.updateAnsi(toDisplay, targetCursorPos);
        oldLines = toDisplay;
    }

    public void redisplay() {
        display(oldLines);
    }

    public void clearBuffer() {
        buf.clear();
        this.lineNo = 0;
        this.oldLineNo = 0;
        // TODO implement for real
        for (int i = 0; i < 5; i++) {
            terminal.puts(InfoCmp.Capability.delete_line);
        }
        display((detailLines != null) ? detailLines : Collections.emptyList());
    }



    public Terminal getTerminal() {
        return terminal;
    }

    public Buffer getBuffer() {
        return buf;
    }
}
