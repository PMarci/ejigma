package ejigma.io;

import ejigma.exception.ArmatureInitException;
import ejigma.exception.ScramblerSettingException;
import ejigma.model.*;
import ejigma.model.type.*;
import ejigma.util.ScrambleResult;
import ejigma.util.Util;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ejigma.model.type.ScramblerType.getScramblerName;
import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

public class KeyBoard implements Runnable {

    private static final String FILTER_COMPLETION = "filterCompletion";
    private static final String SWITCH_PROMPT = ", change them as well? (y/n): ";
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
    private static final String ROTOR_PROMPT_FORMAT = "Enter a type for rotor %d: ";
    private static final String NOT_A_ROTORTYPE_STRING = "Not a valid rotorType";
    private static final String ROTOR_DENY_STRING = "Not setting rotors...";

    private final Terminal terminal;
    private final Enigma enigma;
    private final LineReader selectionReader;
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;

    private String newAlphabetString = null;
    private SelectCompleter selectCompleter;

    private ExecutorService executor;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private String alphabetString;
    private char[] alphabet;

    public KeyBoard(Enigma enigma) {
        setAlphabet(Scrambler.DEFAULT_ALPHABET_STRING);
        this.enigma = enigma;
        this.terminal = enigma.getTerminal();
        this.terminal.handle(Terminal.Signal.INT, signal -> processQuit());
        this.bindingReader = new BindingReader(terminal.reader());
        this.keyMap = initKeyMap(new KeyMap<>());
        this.selectionReader = initSelectionReader(terminal);
    }

    private LineReader initSelectionReader(Terminal terminal) {
        selectCompleter = new SelectCompleter();
        LineReader result = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(selectCompleter)
                .build();
        result.setOpt(LineReader.Option.AUTO_MENU);
        result.setOpt(LineReader.Option.ERASE_LINE_ON_FINISH);
        result.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        result.setOpt(LineReader.Option.COMPLETE_IN_WORD);
        result.setOpt(LineReader.Option.DISABLE_HIGHLIGHTER);
        result.setOpt(LineReader.Option.MOUSE);
        result.setVariable(LineReader.DISABLE_HISTORY, true);
        result.setVariable(FILTER_COMPLETION, false);
        return result;
    }

    public void doStart() {
        ThreadFactory factory = r -> {
            String threadName = "\"The\" thread";
            Thread result = new Thread(r, threadName);
            // the JVM instaquits if only daemon (user) threads are running, so set this to false
            result.setDaemon(false);
            return result;
        };

        this.executor = Executors.newSingleThreadExecutor(factory);
        executor.execute(this);

    }

    private void doStop() throws IOException {
        shuttingDown.set(true);
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.close();

        if (executor != null) {
            executor.shutdown();
            shutdown.set(true);
            executor = null;
        }
    }

    @Override
    public void run() {
        try {
            readFromStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Op {
        ENTER_CHAR,
        NEWLINE,
        SELECT_ENTRY,
        SELECT_ROTOR,
        SELECT_REFLECTOR,
        SELECT_PLUGBOARD,
        RESET_OFFSETS,
        CLEAR_BUFFER,
        DETAIL_MODE_TOGGLE,
        PRINT_ROTORS,
        UP,
        DOWN,
        LEFT,
        RIGHT
//        ,
//        PASTE
    }

    private void readFromStream() {
        Op input;
        while (isRunAllowed()) {
            input = bindingReader.readBinding(keyMap, keyMap, true);
            if (input != null) {
                switch (input) {
                    case ENTER_CHAR:
                        processInput(bindingReader.getLastBinding().charAt(0));
                        break;
                    case NEWLINE:
                        processNewline();
                        break;
//                    case PASTE:
//                        try {
//                            paste();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        break;
                    case SELECT_ENTRY:
                        processSelectEntryIgnoreInterrupt();
                        break;
                    case SELECT_ROTOR:
                        processSelectRotorsIgnoreInterrupt();
                        break;
                    case SELECT_REFLECTOR:
                        processSelectReflectorIgnoreInterrupt();
                        break;
                    case SELECT_PLUGBOARD:
                        processSelectPlugBoardIgnoreInterrupt();
                        break;
                    case RESET_OFFSETS:
                        processResetOffsets();
                        break;
                    case CLEAR_BUFFER:
                        processClearBuffer();
                        break;
                    case DETAIL_MODE_TOGGLE:
                        processDetailModeToggle();
                        break;
                    case PRINT_ROTORS:
                        processPrintRotors();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        processControl(input);
                        break;
                }
            }
        }
    }

    private void processSelectEntryIgnoreInterrupt() {
        try {
            processSelectEntry();
        } catch (UserInterruptException ignored) {
            //ignored
            enigma.getLightBoard().redisplay();
        }
    }

    private void processSelectEntry() {
        ScramblerSelectResponse<EntryWheel, EntryWheelType> newEntryWheelTypeResponse;
        newEntryWheelTypeResponse = promptForScramblerType(EntryWheelType.class);
        EntryWheelType newType = newEntryWheelTypeResponse.getScramblerType();
        String vNewAlphabetString = newType.getAlphabetString();
        if (newEntryWheelTypeResponse.isReselect()) {
            EntryWheelType oldEntryWheel = enigma.getArmature().getEntryWheelType();
            try {
                enigma.forceSetEntryWheel(newType);
                processSelectRotors();
                if (promptForAuto(EntryWheel.class.getSimpleName(), "Reflector")) {
                    enigma.setAutoReflector(vNewAlphabetString);
                } else {
                    processSelectReflector();
                }
                if (promptForAuto(PlugBoard.class.getSimpleName(), "PlugBoard")) {
                    enigma.setAutoPlugBoard(vNewAlphabetString);
                } else {
                    processSelectPlugBoard();
                }
                rebindKeyMap(vNewAlphabetString);
            } catch (UserInterruptException e) {
                enigma.forceSetEntryWheel(oldEntryWheel);
            }
        } else {
            try {
                enigma.setEntryWheel(newType);
            } catch (ArmatureInitException e) {
                selectionReader.printAbove("Can't change entryWheel: " + e.getMessage());
            }
        }
    }

    private void processSelectReflectorIgnoreInterrupt() {
        try {
            processSelectReflector();
        } catch (UserInterruptException ignored) {
            //ignored
            enigma.getLightBoard().redisplay();
        }
    }

    private void processSelectReflector() {
        ScramblerSelectResponse<Reflector, ReflectorType> newReflectorTypeResponse;
        newReflectorTypeResponse = promptForScramblerType(ReflectorType.class);
        ReflectorType newType = newReflectorTypeResponse.getScramblerType();
        String vNewAlphabetString = newType.getAlphabetString();
        if (newReflectorTypeResponse.isReselect()) {
            ReflectorType oldReflectorType = enigma.getArmature().getReflectorType();
            try {
                enigma.forceSetReflector(newType);
                if (promptForAuto(Reflector.class.getSimpleName(), "EntryWheel")) {
                    enigma.setAutoEntryWheel(vNewAlphabetString);
                } else {
                    processSelectEntry();
                }
                processSelectRotors();
                if (promptForAuto(Reflector.class.getSimpleName(), "PlugBoard")) {
                    enigma.setAutoPlugBoard(vNewAlphabetString);
                } else {
                    processSelectPlugBoard();
                }
                rebindKeyMap(vNewAlphabetString);
            } catch (UserInterruptException e) {
                enigma.forceSetReflector(oldReflectorType);
            }
        } else {
            try {
                enigma.setReflector(newType);
            } catch (ArmatureInitException e) {
                selectionReader.printAbove("Can't change reflector: " + e.getMessage());
            }
        }
    }

    private void processSelectPlugBoardIgnoreInterrupt() {
        try {
            processSelectPlugBoard();
        } catch (UserInterruptException ignored) {
            //ignored
            enigma.getLightBoard().redisplay();
        }
    }

    private void processSelectPlugBoard() {
        ScramblerSelectResponse<PlugBoard, PlugBoardConfig> newPlugBoardTypeResponse;
        newPlugBoardTypeResponse = promptForScramblerType(PlugBoardConfig.class);
        PlugBoardConfig newType = newPlugBoardTypeResponse.getScramblerType();
        String vNewAlphabetString = newType.getAlphabetString();
        if (newPlugBoardTypeResponse.isReselect()) {
            PlugBoardConfig oldPlugBoardConfig = (PlugBoardConfig) enigma.getPlugBoard().getType();
            try {
                enigma.forceSetPlugBoard(newType.freshScrambler());
                if (promptForAuto(PlugBoard.class.getSimpleName(), "EntryWheel")) {
                    enigma.setAutoEntryWheel(vNewAlphabetString);
                } else {
                    processSelectEntry();
                }
                processSelectRotors();
                if (promptForAuto(PlugBoard.class.getSimpleName(), "Reflector")) {
                    enigma.setAutoReflector(vNewAlphabetString);
                } else {
                    processSelectReflector();
                }
                rebindKeyMap(vNewAlphabetString);
            } catch (UserInterruptException e) {
                enigma.forceSetPlugBoard(oldPlugBoardConfig.freshScrambler());
            }
        } else {
            try {
                enigma.setPlugBoard(newType);
            } catch (ArmatureInitException | ScramblerSettingException e) {
                selectionReader.printAbove("Can't change PlugBoard: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <S extends Scrambler, T extends ScramblerType<S>> ScramblerSelectResponse<S, T> promptForScramblerType(Class<T> scramblerTypeClass) {
        ReselectHelper<S, T> helper = new ReselectHelper<>();
        String scramblerTypeTypeName = scramblerTypeClass.getSimpleName();
        String scramblerName = getScramblerName(scramblerTypeTypeName);
        String notATypeString = String.format("Not a valid %s", scramblerTypeTypeName);
        String prompt = String.format("Enter a type for the %s: ", scramblerName);
        String plugPrompt = "Enter a wiring for the PlugBoard: ";
        String denyString = String.format("Not setting %s...", scramblerName);
        selectCompleter.setCompleter(scramblerTypeClass);
        Optional<T> scramblerTypeOptional = Optional.empty();
        while (helper.isChoose()) {
            selectionReader.setVariable(FILTER_COMPLETION, false);
            if (scramblerTypeClass.isAssignableFrom(PlugBoardConfig.class)) {
                // forced uppercase conversion
                String initString = selectionReader.readLine(plugPrompt).trim();
                try {
                    PlugBoardConfig plugBoardConfig = PlugBoard.getPlugBoardType(alphabetString, initString);
                    scramblerTypeOptional = (Optional<T>) Optional.of(plugBoardConfig);
                    enigma.validateWithCurrent(plugBoardConfig);
                } catch (ArmatureInitException | ScramblerSettingException e) {
                    helper.setException(e);
                }
            } else {
                String tInput = selectionReader.readLine(prompt).trim();
                scramblerTypeOptional = enigma.getConfigContainer().getScramblerTypes(scramblerTypeClass).stream()
                        .filter(sType -> sType.getName().equals(tInput))
                        .findAny();
            }
            if (scramblerTypeOptional.isPresent()) {
                T newScramblerType = scramblerTypeOptional.get();
                helper.setScramblerTypes((T[]) new ScramblerType[]{newScramblerType});
                this.newAlphabetString = newScramblerType.getAlphabetString();
                selectionReader.setVariable(FILTER_COMPLETION, false);
                try {
                    enigma.getArmature().validateWithCurrent(scramblerTypeClass.cast(newScramblerType));
                } catch (ArmatureInitException e) {
                    helper.setException(e);
                }
                if (helper.getException() != null) {
                    helper.handle(denyString);
                } else {
                    helper.setResponse(new ScramblerSelectResponse<>(newScramblerType, false));
                    helper.setChoose(false);
                }
            } else {
                selectionReader.printAbove(notATypeString);
            }
        }
        return helper.getResponse();
    }

    private void processSelectRotorsIgnoreInterrupt() {
        try {
            processSelectRotors();
        } catch (UserInterruptException ignored) {
            //ignored
            enigma.getLightBoard().redisplay();
        }
    }

    private void processSelectRotors() {
        // rotor no prompt
        int rotorNo;
        selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
        rotorNo = promptForRotorNo();
        selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
        // rotor type prompt
        selectCompleter.setCompleter(RotorType.class);
        ScramblerSelectResponse<Rotor, RotorType> newRotorResponse = promptForRotorTypes(rotorNo);
        RotorType[] newRotorTypes = newRotorResponse.getScramblerTypes();
        String vNewAlphabetString = newRotorTypes[0].getAlphabetString();
        if (newRotorResponse.isReselect()) {
            RotorType[] oldRotorTypes = enigma.getArmature().getRotorTypes();
            try {
                enigma.forceSetRotors(newRotorTypes);
                if (promptForAuto(Rotor.class.getSimpleName(), "EntryWheel")) {
                    enigma.setAutoEntryWheel(vNewAlphabetString);
                } else {
                    processSelectEntry();
                }
                if (promptForAuto(Rotor.class.getSimpleName(), "Reflector")) {
                    enigma.setAutoReflector(vNewAlphabetString);
                } else {
                    processSelectReflector();
                }
                if (promptForAuto(Rotor.class.getSimpleName(), "PlugBoard")) {
                    enigma.setAutoPlugBoard(vNewAlphabetString);
                } else {
                    processSelectPlugBoard();
                }
                rebindKeyMap(vNewAlphabetString);
            } catch (UserInterruptException e) {
                enigma.forceSetRotors(oldRotorTypes);
            }
        } else {
            try {
                enigma.setRotors(newRotorTypes);
            } catch (ArmatureInitException e) {
                selectionReader.printAbove("Can't change rotors: " + e.getMessage());
            }
        }
    }

    private int promptForRotorNo() {
        boolean choose = true;
        String prompt = "Enter number of rotors: ";
        String nanString = "That's not a number.";
        int rotorNo = 0;
        while (choose) {
            String nInput;
            nInput = selectionReader.readLine(prompt);

            Matcher numberMatcher = NUMBERS_PATTERN.matcher(nInput);
            if (numberMatcher.matches()) {
                try {
                    rotorNo = Integer.parseInt(numberMatcher.group(1));
                } catch (NumberFormatException e) {
                    selectionReader.printAbove(nanString);
                    continue;
                }
                choose = false;
            } else {
                selectionReader.printAbove(nanString);
            }
        }
        return rotorNo;
    }

    private ScramblerSelectResponse<Rotor, RotorType> promptForRotorTypes(int rotorNo) {
        RotorType[] newRotorTypes = new RotorType[rotorNo];
        ReselectHelper<Rotor, RotorType> helper = new ReselectHelper<>(newRotorTypes);
        String prompt;
        int i = 0;
        while (i < rotorNo) {
            prompt = String.format(ROTOR_PROMPT_FORMAT, i);
            String tInput = selectionReader.readLine(prompt).trim();
            Optional<RotorType> rotorType = enigma.getConfigContainer().getRotorTypes().stream()
                    .filter(rotorType1 -> rotorType1.getName().equals(tInput)).findAny();
            if (rotorType.isPresent()) {
                RotorType type = rotorType.get();
                String typeAlphabetString = type.getAlphabetString();
                if (i == 0) {
                    newAlphabetString = typeAlphabetString;
                    selectionReader.setVariable(FILTER_COMPLETION, true);
                } else if (newAlphabetString != null && !newAlphabetString.equals(typeAlphabetString)) {
                    selectionReader.printAbove("Incompatible alphabetString.");
                    continue;
                }
                newRotorTypes[i] = type;
                i++;
            } else {
                selectionReader.printAbove(NOT_A_ROTORTYPE_STRING);
            }
        }
        // validate with other scramblers, force reselect until compatible
        try {
            enigma.getArmature().validateWithCurrent(newRotorTypes);
        } catch (ArmatureInitException e) {
            helper.setException(e);
            helper.handle(ROTOR_DENY_STRING);
        }
        if (helper.getException() == null) {
            helper.setResponse(new ScramblerSelectResponse<>(newRotorTypes, false));
        }
        return helper.getResponse();
    }

    private boolean promptForAuto(String unfitType, String autoType) {
        boolean result = false;
        boolean choose = true;
        while (choose) {
            String yn = selectionReader.readLine(String.format(
                    Armature.UNFIT_SCRAMBLER_MSG_FORMAT + ",%nenable autogenerated %s? (y/n)",
                    unfitType,
                    autoType)).trim();
            if ("y".equals(yn) || "Y".equals(yn)) {
                result = true;
                choose = false;
            } else if ("n".equals(yn) || "N".equals(yn)) {
                choose = false;
            }
        }
        return result;
    }


    //    public boolean paste() throws IOException {
//        Clipboard clipboard;
//        try { // May throw ugly exception on system without X
//            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//        }
//        catch (Exception e) {
//            return false;
//        }
//
//        if (clipboard == null) {
//            return false;
//        }
//
//        Transferable transferable = clipboard.getContents(null);
//
//        if (transferable == null) {
//            return false;
//        }
//
//        try {
//            @SuppressWarnings("deprecation")
//            Object content = transferable.getTransferData(DataFlavor.plainTextFlavor);
//
//            // This fix was suggested in bug #1060649 at
//            // http://sourceforge.net/tracker/index.php?func=detail&aid=1060649&group_id=64033&atid=506056
//            // to get around the deprecated DataFlavor.plainTextFlavor, but it
//            // raises a UnsupportedFlavorException on Mac OS X
//
//            if (content == null) {
//                try {
//                    content = new DataFlavor().getReaderForText(transferable);
//                }
//                catch (Exception e) {
//                    // ignore
//                }
//            }
//
//            if (content == null) {
//                return false;
//            }
//
//            String value;
//
//            if (content instanceof Reader) {
//                // TORDO: we might want instead connect to the input stream
//                // so we can interpret individual lines
//                value = "";
//                String line;
//
//                BufferedReader read = new BufferedReader((Reader) content);
//                while ((line = read.readLine()) != null) {
//                    if (value.length() > 0) {
//                        value += "\n";
//                    }
//
//                    value += line;
//                }
//            }
//            else {
//                value = content.toString();
//            }
//
//            if (value == null) {
//                return true;
//            }
//
//            processInput(value);
//
//            return true;
//        }
//        catch (UnsupportedFlavorException e) {
//            Log.error("Paste failed: ", e);
//
//            return false;
//        }
//    }
//    private void processInput(String value) {
//        value.chars().mapToObj(i -> (char) i).forEach(this::processInput);
//    }

    private KeyMap<Op> initKeyMap(KeyMap<Op> keyMap) {
        Set<String> alphabetChars = getUpperAndLower(alphabetString.toCharArray(), Locale.ROOT);
        bind(keyMap, Op.ENTER_CHAR, alphabetChars);
        keyMap.setUnicode(Op.ENTER_CHAR);
        bind(keyMap, Op.NEWLINE, ctrl('M'));
        bind(keyMap, Op.RESET_OFFSETS, ctrl('R'));
        bind(keyMap, Op.DETAIL_MODE_TOGGLE, ctrl('B'));
        bind(keyMap, Op.SELECT_ROTOR, ctrl('I'));
        bind(keyMap, Op.SELECT_ENTRY, ctrl('E'));
        bind(keyMap, Op.SELECT_REFLECTOR, ctrl('F'));
        bind(keyMap, Op.SELECT_PLUGBOARD, ctrl('P'));
        bind(keyMap, Op.CLEAR_BUFFER, ctrl('D'));
//        bind(keyMap, Op.PASTE, ctrl('V'));
        bind(keyMap, Op.UP, key(terminal, InfoCmp.Capability.key_up));
        bind(keyMap, Op.DOWN, key(terminal, InfoCmp.Capability.key_down));
        bind(keyMap, Op.LEFT, key(terminal, InfoCmp.Capability.key_left));
        bind(keyMap, Op.RIGHT, key(terminal, InfoCmp.Capability.key_right));
        return keyMap;
    }

    private void rebindKeyMap(String newAlphabetString) {
        unbind(keyMap, getUpperAndLower(alphabet, Locale.ROOT));
        setAlphabet(newAlphabetString);
        initKeyMap(keyMap);
    }

    private void bind(KeyMap<Op> map, Op op, Iterable<? extends CharSequence> keySeqs) {
        for (CharSequence keySeq : keySeqs) {
            map.bind(op, keySeq);
        }
    }

    private void unbind(KeyMap<Op> map, Iterable<? extends CharSequence> keySeqs) {
        for (CharSequence keySeq : keySeqs) {
            map.unbind(keySeq);
        }
    }

    private void bind(KeyMap<Op> map, Op op, CharSequence... keySeqs) {
        map.bind(op, keySeqs);
    }

    private static Set<String> getUpperAndLower(char[] upperCaseAlphabet, Locale locale) {
        Set<String> upperCaseChars = IntStream.range(0, upperCaseAlphabet.length)
                .mapToObj(value -> String.valueOf(upperCaseAlphabet[value]))
                .collect(Collectors.toSet());
        Set<String> alphabetChars = new HashSet<>(upperCaseChars);
        upperCaseChars.forEach(s -> alphabetChars.add(s.toLowerCase(locale)));
        return alphabetChars;
    }

    private void processInput(char input) {
        ScrambleResult scrambleResult = null;
        if (!Util.containsChar(alphabetString, input)) {
            char upperCase = Character.toUpperCase(input);
            if (Util.containsChar(alphabetString, upperCase)) {
                input = upperCase;
            }
        }
        if (Util.containsChar(alphabetString, input)) {
            int initialResult = alphabetString.indexOf(input);
            scrambleResult = new ScrambleResult(
                    (initialResult != -1) ? initialResult
                                          : 0,
                    alphabetString,
                    input);
        }

        ScrambleResult result = enigma.handle(scrambleResult);
        enigma.getLightBoard().process(result);
    }

    private void processNewline() {
        enigma.getLightBoard().process('\n');
    }

    private void processControl(Op input) {
        switch (input) {
            case LEFT:
                enigma.getLightBoard().getBuffer().moveLeft(1);
                break;
            case RIGHT:
                enigma.getLightBoard().getBuffer().moveRight(1);
                break;
            case UP:
                enigma.getLightBoard().getBuffer().moveUp(1);
                break;
            case DOWN:
                enigma.getLightBoard().getBuffer().moveDown(1);
                break;
            default:
                break;
        }
    }

    private void processResetOffsets() {
        enigma.getArmature().resetOffsets();
        enigma.getLightBoard().statusMsg("Received Ctrl+R, resetting offsets...");
    }

    private void processClearBuffer() {
        enigma.getLightBoard().clearBuffer();
    }

    private void processDetailModeToggle() {
        terminal.puts(InfoCmp.Capability.scroll_forward);
        terminal.puts(InfoCmp.Capability.cursor_down);
        terminal.puts(InfoCmp.Capability.clear_screen);
        enigma.getLightBoard().toggleDetailMode();
    }

    private void processPrintRotors() {
        Rotor[] rotors = enigma.getArmature().getRotors();
        for (int i = 0; i < rotors.length; i++) {
            Rotor rotor = rotors[i];
            // TODO
        }
    }

    private void processQuit() {
        try {
            doStop();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRunAllowed() {
        return !shutdown.get() && !shuttingDown.get();
    }

    public void setAlphabet(String alphabetString) {
        this.alphabetString = alphabetString;
        this.alphabet = alphabetString.toCharArray();
    }

    public static class ScramblerSelectResponse<S extends Scrambler, T extends ScramblerType<S>> {

        private T scramblerType;

        private T[] scramblerTypes;
        private final boolean reselect;
        private final Map<String, Object> satelliteData = new HashMap<>();

        ScramblerSelectResponse(T scramblerType, boolean reselect) {
            this.scramblerType = scramblerType;
            this.reselect = reselect;
        }

        ScramblerSelectResponse(T[] scramblerTypes, boolean reselect) {
            this.scramblerTypes = scramblerTypes;
            this.reselect = reselect;
        }

        T getScramblerType() {
            return scramblerType;
        }

        T[] getScramblerTypes() {
            return scramblerTypes;
        }

        boolean isReselect() {
            return reselect;
        }

    }

    private class ReselectHelper<S extends Scrambler, T extends ScramblerType<S>> {
        private boolean choose = true;
        private Exception exception;
        private ScramblerSelectResponse<S, T> response;
        private T[] scramblerTypes;

        public ReselectHelper() {
            this.response = null;
        }

        public ReselectHelper(T[] scramblerTypes) {
            this();
            this.scramblerTypes = scramblerTypes;
        }

        public void handle(String denyString) {
            selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
            while (choose) {
                String yn = selectionReader.readLine(exception.getMessage() + SWITCH_PROMPT).trim();
                if ("y".equals(yn) || "Y".equals(yn)) {
                    response = scramblerTypes.length > 1 ?
                               new ScramblerSelectResponse<>(scramblerTypes, true) :
                               new ScramblerSelectResponse<>(scramblerTypes[0], true);
                    choose = false;
                } else if ("n".equals(yn) || "N".equals(yn)) {
                    selectionReader.printAbove(denyString);
                    throw new UserInterruptException("");
                }
            }
            selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
        }

        public boolean isChoose() {
            return choose;
        }

        public void setChoose(boolean choose) {
            this.choose = choose;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public ScramblerSelectResponse<S, T> getResponse() {
            return response;
        }

        public void setResponse(ScramblerSelectResponse<S, T> response) {
            this.response = response;
        }

        public T[] getScramblerTypes() {
            return scramblerTypes;
        }

        public void setScramblerTypes(T[] scramblerTypes) {
            this.scramblerTypes = scramblerTypes;
        }
    }

    private class SelectCompleter implements Completer {


        Completer completer;

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            completer.complete(reader, line, candidates);
        }

        void setCompleter(Completer completer) {
            this.completer = completer;
        }

        <T extends ScramblerType<?>> void setCompleter(Class<T> scramblerTypeType) {
            if (scramblerTypeType.isAssignableFrom(ReflectorType.class)) {
                setCompleter(completeFor(enigma.getConfigContainer()::getReflectorTypes));
            } else if (scramblerTypeType.isAssignableFrom(EntryWheelType.class)) {
                setCompleter(completeFor(enigma.getConfigContainer()::getEntryWheelTypes));
            } else if (scramblerTypeType.isAssignableFrom(RotorType.class)) {
                setCompleter(completeFor(enigma.getConfigContainer()::getRotorTypes));
            }
        }

        private <T extends ScramblerType<?>> Completer completeFor(Supplier<List<T>> supplier) {
            return (reader, line, candidates) -> {
                List<T> reflectorTypes = supplier.get();
                candidates.addAll(reflectorTypes.stream()
                                          .filter(reflectorType ->
                                                          !((Boolean) reader.getVariable(FILTER_COMPLETION)) ||
                                                                  (newAlphabetString != null && newAlphabetString
                                                                          .equals(reflectorType.getAlphabetString())))
                                          .map(reflectorType -> new Candidate(reflectorType.getName()))
                                          .collect(Collectors.toSet()));
            };
        }
    }
}
