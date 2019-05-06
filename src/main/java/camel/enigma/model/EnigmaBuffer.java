package camel.enigma.model;

import org.jline.reader.impl.BufferImpl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EnigmaBuffer extends BufferImpl {

    int firstLineToDisplay;
    int firstColumnToDisplay;
    int offsetInLineToDisplay;

    int line;
    List<LinkedList<Integer>> offsets = new ArrayList<>();
    int offsetInLine;
    // cursorCol?
    int column;

}
