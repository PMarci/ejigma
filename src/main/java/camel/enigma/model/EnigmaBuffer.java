package camel.enigma.model;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.nio.charset.Charset;
import java.util.*;

// TODO eventually throw out extension
public class EnigmaBuffer {

    private boolean tabsToSpaces = true;
    private boolean autoIndent = false;
    private boolean wrapping = true;
    private boolean printLineNumbers = false;
    private int tabs = 4;
    private boolean smoothScrolling = true;
    private boolean mark = false;
    private boolean searchToReplace = false;
    public final boolean windowsTerminal;

    protected int matchedLength = -1;
    private boolean atBlanks = false;
    private Size size;

    protected enum CursorMovement {
        RIGHT,
        LEFT,
        STILL
    }

    private List<String> computeFooter() {
        return Collections.emptyList();
    }

    public void clear() {
        this.lines = new ArrayList<>();
        lines.add("");
        line = 0;
        moveToChar(0);
        computeAllOffsets();
    }
    // extra shit end

    String file;
    Charset charset;
    List<String> lines;

    int firstLineToDisplay;
    int firstColumnToDisplay = 0;
    int offsetInLineToDisplay;

    int line;
    List<LinkedList<Integer>> offsets = new ArrayList<>();
    int offsetInLine;
    int column;
    int wantedColumn;
    boolean uncut = false;
    int[] markPos = {-1, -1}; // line, offsetInLine + column

    boolean dirty;

    public EnigmaBuffer(Terminal terminal) {
        this.size = terminal.getSize();
        this.windowsTerminal = terminal.getClass().getSimpleName().endsWith("WinSysTerminal");
        this.lines = new ArrayList<>();
        this.lines.add("");
        computeAllOffsets();
    }

    public int charPosition(int displayPosition) {
        return charPosition(line, displayPosition, CursorMovement.STILL);
    }

    public int charPosition(int displayPosition, CursorMovement move) {
        return charPosition(line, displayPosition, move);
    }

    public int charPosition(int line, int displayPosition) {
        return charPosition(line, displayPosition, CursorMovement.STILL);
    }

    public int charPosition(int line, int displayPosition, CursorMovement move) {
        int out = lines.get(line).length();
        if (!lines.get(line).contains("\t") || displayPosition == 0) {
            out = displayPosition;
        } else if (displayPosition < length(lines.get(line))) {
            int rdiff = 0;
            int ldiff = 0;
            for (int i = 0; i < lines.get(line).length(); i++) {
                int dp = length(lines.get(line).substring(0, i));
                if (move == CursorMovement.LEFT) {
                    if (dp <= displayPosition) {
                        out = i;
                    } else {
                        break;
                    }
                } else if (move == CursorMovement.RIGHT) {
                    if (dp >= displayPosition) {
                        out = i;
                        break;
                    }
                } else if (move == CursorMovement.STILL) {
                    if (dp <= displayPosition) {
                        ldiff = displayPosition - dp;
                        out = i;
                    } else if (dp >= displayPosition) {
                        rdiff = dp - displayPosition;
                        if (rdiff < ldiff) {
                            out = i;
                        }
                        break;
                    }
                }
            }
        }
        return out;
    }

    String blanks(int nb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public void insert(String insert) {
        String text = lines.get(line);
        int pos = charPosition(offsetInLine + column);
        insert = insert.replaceAll("\r\n", "\n");
        insert = insert.replaceAll("\r", "\n");
        if (tabsToSpaces && insert.length() == 1 && insert.charAt(0) == '\t') {
            int len = pos == text.length() ? length(text + insert) : length(text.substring(0, pos) + insert);
            insert = blanks(len - offsetInLine - column);
        }
        if (autoIndent && insert.length() == 1 && insert.charAt(0) == '\n') {
            for (char c : lines.get(line).toCharArray()) {
                if (c == ' ') {
                    insert += c;
                } else if (c == '\t') {
                    insert += c;
                } else {
                    break;
                }
            }
        }
        String mod;
        String tail = "";
        if (pos == text.length()) {
            mod = text + insert;
        } else {
            mod = text.substring(0, pos) + insert;
            tail = text.substring(pos);
        }
        List<String> ins = new ArrayList<>();
        int last = 0;
        int idx = mod.indexOf('\n', last);
        while (idx >= 0) {
            ins.add(mod.substring(last, idx));
            last = idx + 1;
            idx = mod.indexOf('\n', last);
        }
        ins.add(mod.substring(last) + tail);
        int curPos = length(mod.substring(last));
        lines.set(line, ins.get(0));
        offsets.set(line, computeOffsets(ins.get(0)));
        for (int i = 1; i < ins.size(); i++) {
            ++line;
            lines.add(line, ins.get(i));
            offsets.add(line, computeOffsets(ins.get(i)));
        }
        moveToChar(curPos);
        ensureCursorVisible();
        dirty = true;
    }

    void computeAllOffsets() {
        offsets.clear();
        for (String text : lines) {
            offsets.add(computeOffsets(text));
        }
    }

    LinkedList<Integer> computeOffsets(String line) {
        String text = new AttributedStringBuilder().tabs(tabs).append(line).toString();
        int width = size.getColumns() - (printLineNumbers ? 8 : 0);
        LinkedList<Integer> offsets = new LinkedList<>();
        offsets.add(0);
        if (wrapping) {
            int last = 0;
            int prevword = 0;
            boolean inspace = false;
            for (int i = 0; i < text.length(); i++) {
                if (isBreakable(text.charAt(i))) {
                    inspace = true;
                } else if (inspace) {
                    prevword = i;
                    inspace = false;
                }
                if (i == last + width - 1) {
                    if (prevword == last) {
                        prevword = i;
                    }
                    offsets.add(prevword);
                    last = prevword;
                }
            }
        }
        return offsets;
    }

    boolean isBreakable(char ch) {
        return !atBlanks || ch == ' ';
    }

    void moveToChar(int pos) {
        moveToChar(pos, CursorMovement.STILL);
    }

    void moveToChar(int pos, CursorMovement move) {
        if (!wrapping) {
            if (pos > column && pos - firstColumnToDisplay + 1 > width()) {
                firstColumnToDisplay = offsetInLine + column - 6;
            } else if (pos < column && firstColumnToDisplay + 5 > pos) {
                firstColumnToDisplay = Math.max(0, firstColumnToDisplay - width() + 5);
            }
        }
        if (lines.get(line).contains("\t")) {
            int cpos = charPosition(pos, move);
            if (cpos < lines.get(line).length()) {
                pos = length(lines.get(line).substring(0, cpos));
            } else {
                pos = length(lines.get(line));
            }
        }
        offsetInLine = prevLineOffset(line, pos + 1).get();
        column = pos - offsetInLine;
    }

    void delete(int count) {
        while (--count >= 0 && moveRight(1) && backspace(1));
    }

    boolean backspace(int count) {
        while (count > 0) {
            String text = lines.get(line);
            int pos = charPosition(offsetInLine + column);
            if (pos == 0) {
                if (line == 0) {
                    bof();
                    return false;
                }
                String prev = lines.get(--line);
                lines.set(line, prev + text);
                offsets.set(line, computeOffsets(prev + text));
                moveToChar(length(prev));
                lines.remove(line + 1);
                offsets.remove(line + 1);
                count--;
            } else {
                int nb = Math.min(pos, count);
                int curPos = length(text.substring(0, pos - nb));
                text = text.substring(0, pos - nb) + text.substring(pos);
                lines.set(line, text);
                offsets.set(line, computeOffsets(text));
                moveToChar(curPos);
                count -= nb;
            }
            dirty = true;
        }
        ensureCursorVisible();
        return true;
    }

    public boolean moveLeft(int chars) {
        boolean ret = true;
        while (--chars >= 0) {
            if (offsetInLine + column > 0) {
                moveToChar(offsetInLine + column - 1, CursorMovement.LEFT);
            } else if (line > 0) {
                line--;
                moveToChar(length(getLine(line)));
            } else {
                bof();
                ret = false;
                break;
            }
        }
        wantedColumn = column;
        ensureCursorVisible();
        return ret;
    }

    public boolean moveRight(int chars) {
        return moveRight(chars, false);
    }

    int width() {
        return size.getColumns() - (printLineNumbers ? 8 : 0) - (wrapping ? 0 : 1) - (firstColumnToDisplay > 0 ? 1 : 0);
    }

    boolean moveRight(int chars, boolean fromBeginning) {
        if (fromBeginning) {
            firstColumnToDisplay = 0;
            offsetInLine = 0;
            column = 0;
            chars = Math.min(chars, length(getLine(line)));
        }
        boolean ret = true;
        while (--chars >= 0) {
            int len =  length(getLine(line));
            if (offsetInLine + column + 1 <= len) {
                moveToChar(offsetInLine + column + 1, CursorMovement.RIGHT);
            } else if (getLine(line + 1) != null) {
                line++;
                firstColumnToDisplay = 0;
                offsetInLine = 0;
                column = 0;
            } else {
                eof();
                ret = false;
                break;
            }
        }
        wantedColumn = column;
        ensureCursorVisible();
        return ret;
    }

    public void moveDown(int lines) {
        cursorDown(lines);
        ensureCursorVisible();
    }

    public void moveUp(int lines) {
        cursorUp(lines);
        ensureCursorVisible();
    }

    private Optional<Integer> prevLineOffset(int line, int offsetInLine) {
        if (line >= offsets.size()) {
            return Optional.empty();
        }
        Iterator<Integer> it = offsets.get(line).descendingIterator();
        while (it.hasNext()) {
            int off = it.next();
            if (off < offsetInLine) {
                return Optional.of(off);
            }
        }
        return Optional.empty();
    }

    private Optional<Integer> nextLineOffset(int line, int offsetInLine) {
        if (line >= offsets.size()) {
            return Optional.empty();
        }
        return offsets.get(line).stream()
                .filter(o -> o > offsetInLine)
                .findFirst();
    }

    void moveDisplayDown(int lines) {
        int height = size.getRows() - computeHeader().size() - computeFooter().size();
        // Adjust cursor
        while (--lines >= 0) {
            int lastLineToDisplay = firstLineToDisplay;
            if (!wrapping) {
                lastLineToDisplay += height - 1;
            } else {
                int off = offsetInLineToDisplay;
                for (int l = 0; l < height - 1; l++) {
                    Optional<Integer> next = nextLineOffset(lastLineToDisplay, off);
                    if (next.isPresent()) {
                        off = next.get();
                    } else {
                        off = 0;
                        lastLineToDisplay++;
                    }
                }
            }
            if (getLine(lastLineToDisplay) == null) {
                eof();
                return;
            }
            Optional<Integer> next = nextLineOffset(firstLineToDisplay, offsetInLineToDisplay);
            if (next.isPresent()) {
                offsetInLineToDisplay = next.get();
            } else {
                offsetInLineToDisplay = 0;
                firstLineToDisplay++;
            }
        }
    }

    void moveDisplayUp(int lines) {
        int width = size.getColumns() - (printLineNumbers ? 8 : 0);
        while (--lines >= 0) {
            if (offsetInLineToDisplay > 0) {
                offsetInLineToDisplay = Math.max(0, offsetInLineToDisplay - (width - 1));
            } else if (firstLineToDisplay > 0) {
                firstLineToDisplay--;
                offsetInLineToDisplay = prevLineOffset(firstLineToDisplay, Integer.MAX_VALUE).get();
            } else {
                bof();
                return;
            }
        }
    }

    public void cursorDown(int lines) {
        // Adjust cursor
        firstColumnToDisplay = 0;
        while (--lines >= 0) {
            if (!wrapping) {
                if (getLine(line + 1) != null) {
                    line++;
                    offsetInLine = 0;
                    column = Math.min(length(getLine(line)), wantedColumn);
                } else {
                    bof();
                    break;
                }
            } else {
                String txt = getLine(line);
                Optional<Integer> off = nextLineOffset(line, offsetInLine);
                if (off.isPresent()) {
                    offsetInLine = off.get();
                } else if (getLine(line + 1) == null) {
                    eof();
                    break;
                } else {
                    line++;
                    offsetInLine = 0;
                    txt = getLine(line);
                }
                int next = nextLineOffset(line, offsetInLine).orElse(length(txt));
                column = Math.min(wantedColumn, next - offsetInLine);
            }
        }
        moveToChar(offsetInLine + column);
    }

    public void cursorUp(int lines) {
        firstColumnToDisplay = 0;
        while (--lines >= 0) {
            if (!wrapping) {
                if (line > 0) {
                    line--;
                    column = Math.min(length(getLine(line)) - offsetInLine, wantedColumn);
                } else {
                    bof();
                    break;
                }
            } else {
                Optional<Integer> prev = prevLineOffset(line, offsetInLine);
                if (prev.isPresent()) {
                    offsetInLine = prev.get();
                } else if (line > 0) {
                    line--;
                    offsetInLine = prevLineOffset(line, Integer.MAX_VALUE).get();
                    int next = nextLineOffset(line, offsetInLine).orElse(length(getLine(line)));
                    column = Math.min(wantedColumn, next - offsetInLine);
                } else {
                    bof();
                    break;
                }
            }
        }
        moveToChar(offsetInLine + column);
    }

    void ensureCursorVisible() {
        List<AttributedString> header = computeHeader();
        int rwidth = size.getColumns();
        int height = size.getRows() - header.size() - computeFooter().size();

        while (line < firstLineToDisplay
                || line == firstLineToDisplay && offsetInLine < offsetInLineToDisplay) {
            moveDisplayUp(smoothScrolling ? 1 : height / 2);
        }

        while (true) {
            int cursor = computeCursorPosition(header.size() * size.getColumns() + (printLineNumbers ? 8 : 0), rwidth);
            if (cursor >= (height + header.size()) * rwidth) {
                moveDisplayDown(smoothScrolling ? 1 : height / 2);
            } else {
                break;
            }
        }
    }

    void eof() {
    }

    void bof() {
    }

    void resetDisplay() {
        column = offsetInLine + column;
        moveRight(column, true);
    }

    String getLine(int line) {
        return line < lines.size() ? lines.get(line) : null;
    }

    String getTitle() {
        return file != null ? "File: " + file : "New EnigmaBuffer";
    }

    List<AttributedString> computeHeader() {
        return Collections.emptyList();
//        String left = Nano.this.getTitle();
//        String middle = null;
//        String right = dirty ? "Modified" : "        ";
//
//        int width = size.getColumns();
//        int mstart = 2 + left.length() + 1;
//        int mend = width - 2 - 8;
//
//        if (file == null) {
//            middle = "New EnigmaBuffer";
//        } else {
//            int max = mend - mstart;
//            String src = file;
//            if ("File: ".length() + src.length() > max) {
//                int lastSep = src.lastIndexOf('/');
//                if (lastSep > 0) {
//                    String p1 = src.substring(lastSep);
//                    String p0 = src.substring(0, lastSep);
//                    while (p0.startsWith(".")) {
//                        p0 = p0.substring(1);
//                    }
//                    int nb = max - p1.length() - "File: ...".length();
//                    int cut;
//                    cut = Math.max(0, Math.min(p0.length(), p0.length() - nb));
//                    middle = "File: ..." + p0.substring(cut) + p1;
//                }
//                if (middle == null || middle.length() > max) {
//                    left = null;
//                    max = mend - 2;
//                    int nb = max - "File: ...".length();
//                    int cut = Math.max(0, Math.min(src.length(), src.length() - nb));
//                    middle = "File: ..." + src.substring(cut);
//                    if (middle.length() > max) {
//                        middle = middle.substring(0, max);
//                    }
//                }
//            } else {
//                middle = "File: " + src;
//            }
//        }
//
//        int pos = 0;
//        AttributedStringBuilder sb = new AttributedStringBuilder();
//        sb.style(AttributedStyle.INVERSE);
//        sb.append("  ");
//        pos += 2;
//
//        if (left != null) {
//            sb.append(left);
//            pos += left.length();
//            sb.append(" ");
//            pos += 1;
//            for (int i = 1; i < (size.getColumns() - middle.length()) / 2 - left.length() - 1 - 2; i++) {
//                sb.append(" ");
//                pos++;
//            }
//        }
//        sb.append(middle);
//        pos += middle.length();
//        while (pos < width - 8 - 2) {
//            sb.append(" ");
//            pos++;
//        }
//        sb.append(right);
//        sb.append("  \n");
//        if (oneMoreLine) {
//            return Collections.singletonList(sb.toAttributedString());
//        } else {
//            return Arrays.asList(sb.toAttributedString(), new AttributedString("\n"));
//        }
    }

    void highlightDisplayedLine(int curLine, int curOffset, int nextOffset, AttributedStringBuilder line) {
        AttributedString disp = new AttributedStringBuilder().tabs(tabs).append(getLine(curLine)).toAttributedString();
        int[] hls = highlightStart();
        int[] hle = highlightEnd();
        if (hls[0] == -1 || hle[0] == -1) {
            line.append(disp.columnSubSequence(curOffset, nextOffset));
        } else if (hls[0] == hle[0]) {
            if (curLine == hls[0]) {
                if (hls[1] > nextOffset) {
                    line.append(disp.columnSubSequence(curOffset, nextOffset));
                } else if (hls[1] <  curOffset) {
                    if (hle[1] > nextOffset) {
                        line.append(disp.columnSubSequence(curOffset, nextOffset), AttributedStyle.INVERSE);
                    } else if (hle[1] > curOffset) {
                        line.append(disp.columnSubSequence(curOffset, hle[1]), AttributedStyle.INVERSE);
                        line.append(disp.columnSubSequence(hle[1], nextOffset));
                    } else {
                        line.append(disp.columnSubSequence(curOffset, nextOffset));
                    }
                } else {
                    line.append(disp.columnSubSequence(curOffset, hls[1]));
                    if (hle[1] > nextOffset) {
                        line.append(disp.columnSubSequence(hls[1], nextOffset), AttributedStyle.INVERSE);
                    } else {
                        line.append(disp.columnSubSequence(hls[1], hle[1]), AttributedStyle.INVERSE);
                        line.append(disp.columnSubSequence(hle[1], nextOffset));
                    }
                }
            } else {
                line.append(disp.columnSubSequence(curOffset, nextOffset));
            }
        } else {
            if (curLine > hls[0] && curLine < hle[0]) {
                line.append(disp.columnSubSequence(curOffset, nextOffset), AttributedStyle.INVERSE);
            } else if (curLine == hls[0]) {
                if (hls[1] > nextOffset) {
                    line.append(disp.columnSubSequence(curOffset, nextOffset));
                } else if (hls[1] < curOffset) {
                    line.append(disp.columnSubSequence(curOffset, nextOffset), AttributedStyle.INVERSE);
                } else {
                    line.append(disp.columnSubSequence(curOffset, hls[1]));
                    line.append(disp.columnSubSequence(hls[1], nextOffset), AttributedStyle.INVERSE);
                }
            } else if (curLine == hle[0]) {
                if (hle[1] < curOffset) {
                    line.append(disp.columnSubSequence(curOffset, nextOffset));
                } else if (hle[1] > nextOffset) {
                    line.append(disp.columnSubSequence(curOffset, nextOffset), AttributedStyle.INVERSE);
                } else {
                    line.append(disp.columnSubSequence(curOffset, hle[1]), AttributedStyle.INVERSE);
                    line.append(disp.columnSubSequence(hle[1], nextOffset));
                }
            } else {
                line.append(disp.columnSubSequence(curOffset, nextOffset));
            }
        }
    }

    public List<AttributedString> getDisplayedLines(int nbLines) {
        AttributedStyle s = AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK + AttributedStyle.BRIGHT);
        AttributedString cut = new AttributedString("…", s);
        AttributedString ret = new AttributedString("↩", s);

        List<AttributedString> newLines = new ArrayList<>();
        int rwidth = size.getColumns();
        int width = rwidth - (printLineNumbers ? 8 : 0);
        int curLine = firstLineToDisplay;
        int curOffset = offsetInLineToDisplay;
        int prevLine = -1;
//        syntaxHighlighter.reset();
        for (int terminalLine = 0; terminalLine < nbLines; terminalLine++) {
            AttributedStringBuilder line = new AttributedStringBuilder().tabs(tabs);
            if (printLineNumbers && curLine < lines.size()) {
                line.style(s);
                if (curLine != prevLine) {
                    line.append(String.format("%7d ", curLine + 1));
                } else {
                    line.append("      ‧ ");
                }
                line.style(AttributedStyle.DEFAULT);
                prevLine = curLine;
            }
            if (curLine >= lines.size()) {
                // Nothing to do
            } else if (!wrapping) {
                AttributedString disp = new AttributedStringBuilder().tabs(tabs).append(getLine(curLine)).toAttributedString();
                if (this.line == curLine) {
                    int cutCount = 1;
                    if (firstColumnToDisplay > 0) {
                        line.append(cut);
                        cutCount = 2;
                    }
                    if (disp.columnLength() - firstColumnToDisplay >= width - (cutCount - 1)*cut.columnLength()) {
                        highlightDisplayedLine(curLine, firstColumnToDisplay
                                , firstColumnToDisplay + width - cutCount*cut.columnLength(), line);
                        line.append(cut);
                    } else {
                        highlightDisplayedLine(curLine, firstColumnToDisplay, disp.columnLength(), line);
                    }
                } else {
                    if (disp.columnLength() >= width) {
                        highlightDisplayedLine(curLine, 0, width - cut.columnLength(), line);
                        line.append(cut);
                    } else {
                        highlightDisplayedLine(curLine, 0, disp.columnLength(), line);
                    }
                }
                curLine++;
            } else {
                Optional<Integer> nextOffset = nextLineOffset(curLine, curOffset);
                if (nextOffset.isPresent()) {
                    highlightDisplayedLine(curLine, curOffset, nextOffset.get(), line);
                    line.append(ret);
                    curOffset = nextOffset.get();
                } else {
                    highlightDisplayedLine(curLine, curOffset, Integer.MAX_VALUE, line);
                    curLine++;
                    curOffset = 0;
                }
            }
            line.append('\n');
            newLines.add(line.toAttributedString());
        }
        return newLines;
    }

    public void moveTo(int x, int y) {
        if (printLineNumbers) {
            x = Math.max(x - 8, 0);
        }
        line = firstLineToDisplay;
        offsetInLine = offsetInLineToDisplay;
        wantedColumn = x;
        cursorDown(y);
    }

    public void gotoLine(int x, int y) {
        line = y < lines.size() ? y : lines.size() - 1;
        x = Math.min(x, length(lines.get(line)));
        firstLineToDisplay = line > 0 ? line - 1 : line;
        offsetInLine = 0;
        offsetInLineToDisplay = 0;
        column = 0;
        moveRight(x);
    }

    public int getDisplayedCursor() {
        return computeCursorPosition(printLineNumbers ? 8 : 0, size.getColumns() + 1);
    }

    public int computeCursorPosition(int cursor, int rwidth) {
        int cur = firstLineToDisplay;
        int off = offsetInLineToDisplay;
        while (true) {
            if (cur < line || off < offsetInLine) {
                if (!wrapping) {
                    cursor += rwidth;
                    cur++;
                } else {
                    cursor += rwidth;
                    Optional<Integer> next = nextLineOffset(cur, off);
                    if (next.isPresent()) {
                        off = next.get();
                    } else {
                        cur++;
                        off = 0;
                    }
                }
            } else if (cur == line) {
                if (!wrapping && column > firstColumnToDisplay + width()) {
                    while (column > firstColumnToDisplay + width()) {
                        firstColumnToDisplay += width();
                    }
                }
                cursor += column - firstColumnToDisplay + (firstColumnToDisplay > 0 ? 1 : 0);
                break;
            } else {
                throw new IllegalStateException();
            }
        }
        return cursor;
    }

    char getCurrentChar() {
        String str = lines.get(line);
        if (column + offsetInLine < str.length()) {
            return str.charAt(column + offsetInLine);
        } else if (line < lines.size() - 1) {
            return '\n';
        } else {
            return 0;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void prevWord() {
        while (Character.isAlphabetic(getCurrentChar())
                && moveLeft(1));
        while (!Character.isAlphabetic(getCurrentChar())
                && moveLeft(1));
        while (Character.isAlphabetic(getCurrentChar())
                && moveLeft(1));
        moveRight(1);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void nextWord() {
        while (Character.isAlphabetic(getCurrentChar())
                && moveRight(1));
        while (!Character.isAlphabetic(getCurrentChar())
                && moveRight(1));
    }

    public void beginningOfLine() {
        column = offsetInLine = 0;
        wantedColumn = 0;
        ensureCursorVisible();
    }

    public void endOfLine() {
        int x = length(lines.get(line));
        moveRight(x, true);
    }

    public void prevPage() {
        int height = size.getRows() - computeHeader().size() - computeFooter().size();
        scrollUp(height - 2);
        column = 0;
        firstLineToDisplay = line;
        offsetInLineToDisplay = offsetInLine;
    }

    public void nextPage() {
        int height = size.getRows() - computeHeader().size() - computeFooter().size();
        scrollDown(height - 2);
        column = 0;
        firstLineToDisplay = line;
        offsetInLineToDisplay = offsetInLine;
    }

    public void scrollUp(int lines) {
        cursorUp(lines);
        moveDisplayUp(lines);
    }

    public void scrollDown(int lines) {
        cursorDown(lines);
        moveDisplayDown(lines);
    }

    public void firstLine() {
        line = 0;
        offsetInLine = column = 0;
        ensureCursorVisible();
    }

    public void lastLine() {
        line = lines.size() - 1;
        offsetInLine = column = 0;
        ensureCursorVisible();
    }

    protected int[] highlightStart() {
        int[] out = {-1, -1};
        if (mark) {
            out = getMarkStart();
        } else if (searchToReplace) {
            out[0] = line;
            out[1] = offsetInLine + column;
        }
        return out;
    }

    protected int[] highlightEnd() {
        int[] out = {-1, -1};
        if (mark) {
            out = getMarkEnd();
        } else if (searchToReplace && matchedLength > 0) {
            out[0] = line;
            int col = charPosition(offsetInLine + column) + matchedLength;
            if (col < lines.get(line).length()) {
                out[1] = length(lines.get(line).substring(0, col));
            } else {
                out[1] = length(lines.get(line));
            }
        }
        return out;
    }

    public int length(String line) {
        return new AttributedStringBuilder().tabs(tabs).append(line).columnLength();
    }

    int[] getMarkStart() {
        int[] out = {-1, -1};
        if (!mark) {
            return out;
        }
        if (markPos[0] > line || (markPos[0] == line && markPos[1] > offsetInLine + column) ) {
            out[0] = line;
            out[1] = offsetInLine + column;
        } else {
            out = markPos;
        }
        return out;
    }

    int[] getMarkEnd() {
        int[] out = {-1, -1};
        if (!mark) {
            return out;
        }
        if (markPos[0] > line || (markPos[0] == line && markPos[1] > offsetInLine + column) ) {
            out = markPos;
        } else {
            out[0] = line;
            out[1] = offsetInLine + column;
        }
        return out;
    }

    void replaceFromCursor(int chars, String string) {
        int pos = charPosition(offsetInLine + column);
        String text = lines.get(line);
        String mod = text.substring(0, pos) + string;
        if (chars + pos < text.length()) {
            mod += text.substring(chars + pos);
        }
        lines.set(line, mod);
        dirty = true;
    }

}
