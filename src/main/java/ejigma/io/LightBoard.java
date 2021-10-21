package ejigma.io;

import ejigma.model.EnigmaBuffer;
import ejigma.util.ScrambleResult;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LightBoard {

    private final Terminal terminal;
    private final Status status;
    private final Size size;
    private final EnigmaBuffer buf;
    private final Display display;

    private List<AttributedString> oldLines = Collections.emptyList();
    private ScrambleResult oldResult;

    private List<AttributedString> detailLines = Collections.emptyList();

    private boolean detailMode;

    public LightBoard(Terminal terminal) {
        this.terminal = terminal;
        size = new Size();
        size.copy(terminal.getSize());
        display = new Display(terminal, true);
        status = Status.getStatus(terminal);
        display.setDelayLineWrap(false);
        buf = new EnigmaBuffer(terminal);
        terminal.handle(Signal.WINCH, this::handleWinch);
    }

    void process(ScrambleResult scrambleResult) {
        oldResult = scrambleResult;
        process(scrambleResult.getResultAsChar());
    }

    void process(char c) {
        display.clear();
        buf.insert(String.valueOf(c));
        display();
    }

    public void display() {
        List<AttributedString> toDisplay;
        if (isDetailMode()) {
            prepareDetailModeLines(oldResult);
            toDisplay = new ArrayList<>(detailLines);
        } else {
            toDisplay = new ArrayList<>();
        }
        int nbLines = size.getRows() - toDisplay.size()/* - header.size() - footer.size()*/;
        toDisplay.addAll(buf.getDisplayedLines(nbLines));
        display(toDisplay);
    }

    public void display(List<AttributedString> toDisplay) {
        // TODO needed?
        size.copy(terminal.getSize());
        resetDisplay();
        // not updated with fullscreen=false
        int cursorPos = buf.getDisplayedCursor();
        // replacement
        List<AttributedString> header = isDetailMode() ? detailLines : Collections.emptyList();
        List<AttributedString> footer = Collections.emptyList();

        int nbLines = size.getRows() - header.size() - footer.size();
        List<AttributedString> newLines = buf.getDisplayedLines(nbLines);
        newLines.addAll(0, header);
        newLines.addAll(footer);

        // Compute cursor position
        int cursor;
        cursor = size.cursorPos(header.size(), cursorPos);
        display.update(newLines, cursor);
//        if (buf.windowsTerminal) {
//            resetDisplay();
//        }
        // status needs to be drawn over display if fullscreen=false
        // TODO see if any downside to forcing update every time with reset
//        status.reset();
//        status.update(Arrays.asList(createStatusStringL1(), createStatusStringL2()));
        this.oldLines = toDisplay;
    }

    private void resetDisplay() {
        display.clear();
        display.resize(size.getRows(), size.getColumns());
    }

//    public AttributedString createStatusStringL1() {
//        return new AttributedStringBuilder()
//                .append("intbxPos: ")
//                .append(String.valueOf(buf.getIntColNo()))
//                .append(", intbyPos: ")
//                .append(String.valueOf(buf.getIntLineNo()))
//                .append("  cPos: ")
//                .append(String.valueOf(cursorPos))
//                .append(", bPos: ")
//                .append(String.valueOf(buf.cursor()))
//                .append(", lineLen: ")
//                .append(String.valueOf(buf.getCurrLineLen()))
//                .toAttributedString();
//    }

    public AttributedString createStatusStringL2() {
        return new AttributedString("");
//        AttributedStyle style =
//                new AttributedStyle().background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK);
//        return new AttributedStringBuilder()
//                .append(armature.getOffsetString(), style)
//                .append("absbxPos: ")
//                .append(String.valueOf(buf.absColPos()))
//                .append(", absbyPos: ")
//                .append(String.valueOf(buf.absRowPos()))
//                .append(", len: ")
//                .append(String.valueOf(buf.length()))
//                .append(", sCols: ")
//                .append(String.valueOf(size.getColumns()))
//                .append(", sRows: ")
//                .append(String.valueOf(size.getRows()))
//                .toAttributedString();
    }

    public void statusMsg(String string) {
        List<AttributedString> result = new ArrayList<>();
        result.add(new AttributedString(string));
        result.add(createStatusStringL2());
        status.update(result);
    }

    public void redisplay() {
        display(oldLines);
    }

    private void prepareDetailModeLines(ScrambleResult scrambleResult) {
        if (scrambleResult != null) {
            this.detailLines = scrambleResult.printHistory();
        } else {
            this.detailLines = Collections.emptyList();
            statusMsg("No scrambled characters, can't display detailLines");
        }
    }

    public void clearBuffer() {
        buf.clear();
        display();
    }

    public void handleWinch(Signal signal) {
        size.copy(terminal.getSize());
//            TODO here's what jline Nano does with WINCH
//            buffer.computeAllOffsets();
//            buffer.moveToChar(buffer.offsetInLine + buffer.column);
        resetDisplay();
        display();
    }


    void toggleDetailMode() {
        setDetailMode(!isDetailMode());
        display();
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

    public EnigmaBuffer getBuffer() {
        return buf;
    }
}
