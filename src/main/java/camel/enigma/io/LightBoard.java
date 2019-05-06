package camel.enigma.io;

import camel.enigma.model.Armature;
import camel.enigma.model.EnigmaBuffer;
import camel.enigma.util.ScrambleResult;
import org.jline.reader.Buffer;
import org.jline.terminal.Cursor;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;
import org.jline.utils.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LightBoard {

    private static final Logger logger = LoggerFactory.getLogger(LightBoard.class);

    private Terminal terminal;
    private Status status;
    private Size size;
    private final EnigmaBuffer buf;
    private Display display;
    private int lineNo;
    private int oldLineNo;
    private int len;
    private Cursor pos;

    private List<String> oldLines = Collections.emptyList();
    private ScrambleResult oldResult;

    private List<String> detailLines = Collections.emptyList();

    private boolean detailMode;

    private Armature armature;

    public LightBoard(Terminal terminal, Armature armature) {
        this.terminal = terminal;
        this.armature = armature;
        size = new Size();
        size.copy(terminal.getSize());
        display = new Display(terminal, false);
        status = Status.getStatus(terminal);
        // not setting this to true worsens linebreak behavior (tested in CMD thus far)
        display.setDelayLineWrap(true);
        buf = new EnigmaBuffer();
        clearBuffer();
    }

    void process(ScrambleResult scrambleResult) {
        List<String> toDisplay;
        // TODO erase only oldlines amount also don't slam the screen
        terminal.puts(InfoCmp.Capability.clear_screen);
        if (isDetailMode()) {
            toDisplay = prepareDetailModeLines(scrambleResult);
        } else {
            String characterAsString = String.valueOf(scrambleResult.getResultAsChar());
            detailLines = Collections.emptyList();
            buf.write(characterAsString);
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
        oldResult = scrambleResult;
    }

    public void display(List<String> toDisplay) {
        size.copy(terminal.getSize());
        display.clear();
        display.reset();
        int columns;
        int defaultColumns = 80;
        int targetColumns = (columns = size.getColumns()) > 0 ? columns : defaultColumns;
        display.resize(size.getRows(), targetColumns);
        int targetRow = detailLines.size();
        int cursorPos = buf.cursor() + size.cursorPos(targetRow, 0);
        int targetCursorPos = (cursorPos) > -1 ? cursorPos : 0;
        status.update(Collections.singletonList(AttributedString.fromAnsi(armature.getOffsetString())));
        display.updateAnsi(toDisplay, targetCursorPos);
        oldLines = toDisplay;
    }

    public void redisplay() {
        display(oldLines);
    }

    private List<String> prepareDetailModeLines(ScrambleResult scrambleResult) {
        List<String> lines;
        List<String> toDisplay;
        lines = scrambleResult.printHistory();
        char detailModeChar = scrambleResult.getResultAsChar();
        detailLines = lines;
        toDisplay = new ArrayList<>(detailLines);
        buf.write(detailModeChar);
        toDisplay.add(buf.toString());
        return toDisplay;
    }

    public void clearBuffer() {
        buf.clear();
        this.lineNo = 0;
        this.oldLineNo = 0;
        // TODO implement for real
        for (int i = 0; i < 5; i++) {
            terminal.puts(InfoCmp.Capability.delete_line);
        }
        display(detailLines);
    }

    void toggleDetailMode() {
        setDetailMode(!isDetailMode());
        if (oldResult != null) {
            process(oldResult);
        } else {
            // TODO replace
            logger.info("No scrambled characters, can't display detailLines");
        }
    }

    private boolean isDetailMode() {
        return detailMode;
    }

    private void setDetailMode(boolean detailMode) {
        this.detailMode = detailMode;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public Buffer getBuffer() {
        return buf;
    }
}
