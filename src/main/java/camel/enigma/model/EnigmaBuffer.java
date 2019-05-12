package camel.enigma.model;

import org.jline.terminal.Size;
import org.jline.utils.AttributedStringBuilder;

import java.util.LinkedList;
import java.util.List;

// TODO eventually throw out extension
public class EnigmaBuffer {

    private Size size;
    private LinkedList<String> lines;
    private LinkedList<Integer> offsets;
    private int offsetInLine;
    // cursorCol?
    private int cursor;

    private int intColNo;
    private int intLineNo;

    private int absColNo;
    private int absLineNo;

    public EnigmaBuffer(Size size) {
        this.size = size;
        this.lines = new LinkedList<>();
        lines.add("");
        this.offsets = new LinkedList<>();
        this.offsetInLine = 0;
        this.cursor = 0;
        this.intColNo = 0;
        this.intLineNo = 0;
    }

    public List<String> getLines() {
        return lines;
    }

    // get absolute column relative to first char (0,0) of buffer, including display width breaks
    public int absColPos() {
        return absColNo;
    }

    // get absolute row relative to first char (0,0) of buffer, including display width breaks
    public int absRowPos() {
        return absLineNo;
    }

    public int cursor() {
        return cursor;
    }

    public void write(char c) {
        String line = getLine(intLineNo);
        int lineLen = length(line);
        String modified;
        if (c == '\n') {
            lines.add("");
            intLineNo++;
            intColNo = 0;
        } else if (intColNo == lineLen) {
            modified = line + c;
            lines.set(intLineNo, modified);
            cursor++;
            intColNo++;
        } else if (intColNo < lineLen) {
            // TODO get rid of warning
            modified = line.substring(0, intColNo) + c + line.substring(intColNo);
            lines.set(intLineNo, modified);
            cursor++;
            intColNo++;
        } else {
            throw new RuntimeException("nooooooooo");
        }
        calculateAbsOffsets();
    }

    public int length() {
        int result = 0;
        for (int i = 0; i < lines.size(); i++) {
            result += getLineLength(i);
        }
        return result;
    }

    public boolean clear() {
        boolean cleared = false;
        if (!lines.isEmpty()) {
            lines.clear();
            intColNo = 0;
            intLineNo = 0;
            cursor = 0;
            lines.add("");
            cleared = true;
        }
        calculateAbsOffsets();
        return cleared;
    }

    public boolean moveLeft(int i) {
        boolean canMove = true;
        while (--i >= 0) {
            if (intColNo > 0) {
                intColNo--;
                cursor--;
            } else if (intLineNo > 0) {
                intLineNo--;
                intColNo = getLineLength(intLineNo);
                cursor--;
            } else {
                // on first char
                canMove = false;
            }
        }
        calculateAbsOffsets();
        return canMove;
    }

    public boolean moveRight(int i) {
        boolean canMove = true;
        while (--i >= 0) {
            int length = getLineLength(intLineNo);
            if (intColNo < length) {
                intColNo++;
                cursor++;
            } else if (intColNo == length && intLineNo < lines.size() - 1) {
                intLineNo++;
                intColNo = 0;
                cursor++;
            } else {
                canMove = false;
            }
        }
        calculateAbsOffsets();
        return canMove;
    }

    private void calculateAbsOffsets() {
        absColNo = intColNo % (size.getColumns());
        absLineNo = something();
    }

    private int brokenLength(String s) {
        return length(s) % size.getColumns();
    }

    private int something() {
        int vLineNo = intLineNo;
        int sizeColumns = size.getColumns();
        int result = intColNo / sizeColumns;
        vLineNo--;
        while (vLineNo >= 0) {
            int lineLength = getLineLength(vLineNo);
            result += (lineLength / sizeColumns) + 1;
            vLineNo--;
        }
        return result;
    }

    public int absCursorPos() {
        int sizeColumns = size.getColumns();
        return sizeColumns * absRowPos() + absColPos();
    }

    private int brokenHeight(int lineNo) {
        int result = 0;
        int lineLength = getLineLength(lineNo);
        int sizeLength = size.getColumns();
        while (lineLength >= sizeLength) {
            lineLength -= sizeLength;
            result++;
        }
        return result;
    }

    // from Nano.buffer, without tabs
    private int length(String line) {
        return (line != null) ? new AttributedStringBuilder().append(line).columnLength() : 0;
    }

    private String getLine(int line) {
        return line < lines.size() ? lines.get(line) : "";
    }

    public int getCurrLineLen() {
        return getLineLength(intLineNo);
    }

    private int getLineLength(int intLineNo) {
        return length(getLine(intLineNo));
    }

    public int getIntColNo() {
        return intColNo;
    }

    public int getIntLineNo() {
        return intLineNo;
    }
}
