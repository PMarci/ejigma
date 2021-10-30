package ejigma.io;

import ejigma.exception.ArmatureInitException;
import ejigma.exception.ScramblerSettingException;
import ejigma.model.component.*;
import ejigma.model.type.*;
import ejigma.model.type.auto.AutoEntryWheelType;
import ejigma.model.type.auto.AutoPlugBoardConfig;
import ejigma.model.type.auto.AutoReflectorType;
import ejigma.util.ScrambleResult;
import ejigma.util.ScramblerSelectResponse;
import ejigma.util.Util;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.List;
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
import static org.jline.reader.LineReader.*;
import static org.jline.reader.LineReader.Option.MOUSE;

public class KeyBoard implements Runnable {

    private static final String FILTER_COMPLETION = "filterCompletion";
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
    private static final String ROTOR_PROMPT_FORMAT = "Enter a type for rotor %d: ";
    private static final String NOT_A_ROTORTYPE_STRING = "Not a valid rotorType";
    private static final String ROTOR_DENY_STRING = "Not setting rotors...";
    private static final String SWITCH_PROMPT = ", change them (y) or quit selection (n)? (y/n): ";

    private final Terminal terminal;
    private final Enigma enigma;
    private final LineReader selectionReader;
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private ExecutorService executor;

    private char[] alphabet;
    private String alphabetString;
    private String newAlphabetString = null;

    private SelectCompleter selectCompleter;

    private ScramblerCache scramblerCache;

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
        result.setOpt(Option.AUTO_MENU);
        result.setOpt(Option.ERASE_LINE_ON_FINISH);
        result.setOpt(Option.DISABLE_EVENT_EXPANSION);
        result.setOpt(Option.COMPLETE_IN_WORD);
        result.setOpt(Option.DISABLE_HIGHLIGHTER);
        result.setOpt(MOUSE);
        result.setVariable(DISABLE_HISTORY, true);
        result.setVariable(FILTER_COMPLETION, false);
        return result;
    }

    public void doStart() {
        ThreadFactory factory = r -> {
            String threadName = KeyBoard.class.getSimpleName();
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
        YANK_ARMATURE,
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
                    case SELECT_ENTRY:
                        processSelectScramblerIgnoreInterrupt(EntryWheelType.class);
                        break;
                    case SELECT_ROTOR:
                        processSelectScramblerIgnoreInterrupt(RotorType.class);
                        break;
                    case SELECT_REFLECTOR:
                        processSelectScramblerIgnoreInterrupt(ReflectorType.class);
                        break;
                    case SELECT_PLUGBOARD:
                        processSelectScramblerIgnoreInterrupt(PlugBoardConfig.class);
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
                    case YANK_ARMATURE:
                        processYankArmature();
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

    private <
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> void processSelectScramblerIgnoreInterrupt(Class<T> scramblerTypeClass) {
        try {
            scramblerCache = new ScramblerCache(enigma);
            processSelectScrambler(scramblerTypeClass);
        } catch (UserInterruptException ignored) {
            //ignored
            enigma.resetFromCache(scramblerCache);
            enigma.getLightBoard().redisplay();
            scramblerCache = null;
        }
    }

    @SuppressWarnings("unchecked")
    private <
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> void processSelectScrambler(Class<T> scramblerTypeClass) {
        T type = null;
        T firstType = null;
        int rotorNo = -1;
        int currentRNo = 0;
        boolean rotorNoSet = false;
        ScramblerSelectResponse response = null;
        boolean promptReselect = true;
        List<RotorType> newRTypes = new ArrayList<>();
        this.newAlphabetString = null;
        do {
            // TODO messages
            // correct reselection at the end of each branch
            Class<T> typeToSelect = (response != null) ? (Class<T>) response.next() : scramblerTypeClass;
            if (RotorType.class.isAssignableFrom(typeToSelect)) {
                if (!rotorNoSet) {
                    rotorNo = promptForRotorNo();
                    rotorNoSet = true;
                }
                RotorType currentRType = (RotorType) promptForScramblerType(typeToSelect);
                type = (T) currentRType;
                newRTypes.add(currentRType);
                try {
                    if (response == null) {
                        response = new ScramblerSelectResponse(currentRType, alphabetString, rotorNo);
                        firstType = type;
                    } else {
                        response.set(currentRType, currentRNo, alphabetString);
                        Armature.validateRotorTypes(newRTypes);
                        response.validateNonNullTypes();
                    }
                    currentRNo++;
                } catch (ArmatureInitException e) {
                    selectionReader.printAbove(e.getMessage());
                    newRTypes.remove(currentRType);
                    response.reselect(currentRNo);
                }
            } else {
                rotorNo = enigma.getArmature().getRotors().length;
                try {
                    if (response == null) {
                        type = promptForScramblerType(typeToSelect);
                        response = new ScramblerSelectResponse(type, alphabetString, rotorNo);
                        firstType = type;
                    } else {
                        // TODO unify with rotor branch?
                        boolean auto = promptForAuto(firstType.getClass(), typeToSelect);
                        type = (auto) ?
                               getAutoScrambler(typeToSelect, response.getAlphabetString()) :
                               promptForScramblerType(typeToSelect);
                        response.set(type, alphabetString);
                        response.validateNonNullTypes();
                    }
                } catch (ArmatureInitException e) {
                    selectionReader.printAbove(e.getMessage());
                    response.reselect(type);
                }
            }
            if (promptReselect) {
                promptReselect(type.getAlphabetString(), type.getClass().getSimpleName());
                promptReselect = false;
            }
        } while (response.hasNext());
        response.apply(enigma);
        rebindKeyMap(response.getAlphabetString());
    }

    @SuppressWarnings("unchecked")
    private <
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> T promptForScramblerType(Class<T> scramblerTypeClass) {
        String prompt = String.format("Enter a type for the %s: ",
                                      getScramblerName(scramblerTypeClass.getSimpleName()));
        String plugPrompt = "Enter a wiring for the PlugBoard: ";
        selectCompleter.setCompleter(scramblerTypeClass);
        Optional<T> scramblerTypeOptional = Optional.empty();
        while (scramblerTypeOptional.isEmpty()) {
            if (scramblerTypeClass.isAssignableFrom(PlugBoardConfig.class)) {
                boolean completionFilterSetting =
                        (boolean) Optional.ofNullable(selectionReader.getVariable(FILTER_COMPLETION))
                                .orElse(false);
                boolean completionSetting =
                        (boolean) Optional.ofNullable(selectionReader.getVariable(DISABLE_COMPLETION))
                                .orElse(false);
                selectionReader.setVariable(FILTER_COMPLETION, false);
                selectionReader.setVariable(DISABLE_COMPLETION, true);
                String initString = selectionReader.readLine(plugPrompt).trim();
                try {
                    PlugBoardConfig plugBoardConfig = new PlugBoardConfig(
                            (newAlphabetString != null) ?
                            newAlphabetString :
                            alphabetString,
                            initString);
                    plugBoardConfig.unsafeScrambler().validatePlugBoard();
                    scramblerTypeOptional = (Optional<T>) Optional.of(plugBoardConfig);
                } catch (ScramblerSettingException e) {
                    selectionReader.printAbove(e.getMessage());
                }
                selectionReader.setVariable(FILTER_COMPLETION, completionFilterSetting);
                selectionReader.setVariable(DISABLE_COMPLETION, completionSetting);
            } else {
                selectionReader.setVariable(FILTER_COMPLETION, newAlphabetString != null);
                String tInput = selectionReader.readLine(prompt).trim();
                scramblerTypeOptional = enigma.getConfigContainer().getScramblerTypes(scramblerTypeClass).stream()
                        .filter(sType -> sType.getName().equals(tInput))
                        .findAny();
            }
            if (scramblerTypeOptional.isPresent()) {
                T newScramblerType = scramblerTypeOptional.get();
                this.newAlphabetString = newScramblerType.getAlphabetString();
            }
        }
        return scramblerTypeOptional.get();
    }

    private void promptReselect(String alphabetString, String scramblerTypeName) {
        if (!alphabetString.equals(this.alphabetString)) {
            selectionReader.setVariable(DISABLE_COMPLETION, true);
            boolean choose = true;
            while (choose) {
                String yn = selectionReader.readLine(
                        "Other scramblers don't fit selected " + scramblerTypeName +
                                SWITCH_PROMPT).trim();
                if ("y".equals(yn) || "Y".equals(yn)) {
                    choose = false;
                } else if ("n".equals(yn) || "N".equals(yn)) {
                    throw new UserInterruptException("");
                }
            }
            selectionReader.setVariable(DISABLE_COMPLETION, false);
        }
    }

    private int promptForRotorNo() {
        boolean choose = true;
        String prompt = "Enter number of rotors: ";
        String nanString = "That's not nonzero number.";
        int rotorNo = 0;
        selectionReader.setVariable(DISABLE_COMPLETION, true);
        while (choose) {
            String nInput;
            nInput = selectionReader.readLine(prompt).trim();

            Matcher numberMatcher = NUMBERS_PATTERN.matcher(nInput);
            if (numberMatcher.matches()) {
                try {
                    rotorNo = Integer.parseInt(numberMatcher.group(1));
                } catch (NumberFormatException e) {
                    selectionReader.printAbove(nanString);
                    continue;
                }
                if (rotorNo > 0) {
                    choose = false;
                } else {
                    selectionReader.printAbove(nanString);
                }
            } else {
                selectionReader.printAbove(nanString);
            }
        }
        selectionReader.setVariable(DISABLE_COMPLETION, false);
        return rotorNo;
    }

    // TODO think about collapsing this into the ConfigContainer by initializing auto types into the collections
    private <
            U extends ScramblerType<?, ?>,
            A extends ScramblerType<?, ?>> boolean promptForAuto(Class<U> unfitClass, Class<A> autoClass) {
        boolean result = false;
        boolean choose = true;
        while (choose) {
            String yn = selectionReader.readLine(
                            String.format(
                                    Armature.UNFIT_SCRAMBLER_MSG_FORMAT + ",%nenable autogenerated %s? (y/n)",
                                    ScramblerType.getScramblerName(unfitClass.getSimpleName()),
                                    ScramblerType.getScramblerName(autoClass.getSimpleName())))
                    .trim();
            if ("y".equals(yn) || "Y".equals(yn)) {
                result = true;
                choose = false;
            } else if ("n".equals(yn) || "N".equals(yn)) {
                choose = false;
            }
        }
        return result;
    }

    private static <
            S extends Scrambler<S, T>,
            T extends ScramblerType<S, T>> T getAutoScrambler(Class<T> scramblerTypeClass, String alphabetString) {
        T result;
        if (ReflectorType.class.isAssignableFrom(scramblerTypeClass)) {
            result = (T) new AutoReflectorType(alphabetString);
        } else if (EntryWheelType.class.isAssignableFrom(scramblerTypeClass)) {
            result = (T) new AutoEntryWheelType(alphabetString);
        } else if (PlugBoardConfig.class.isAssignableFrom(scramblerTypeClass)) {
            result = (T) AutoPlugBoardConfig.create(alphabetString);
        } else {
            throw new IllegalArgumentException(String.format("Can't generate auto scrambler for %s",
                                                             scramblerTypeClass.getSimpleName()));
        }
        return result;
    }

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
        bind(keyMap, Op.YANK_ARMATURE, ctrl('Y'));
        bind(keyMap, Op.UP, key(terminal, InfoCmp.Capability.key_up));
        bind(keyMap, Op.DOWN, key(terminal, InfoCmp.Capability.key_down));
        bind(keyMap, Op.LEFT, key(terminal, InfoCmp.Capability.key_left));
        bind(keyMap, Op.RIGHT, key(terminal, InfoCmp.Capability.key_right));
        unbind(keyMap, Collections.singleton(LineReader.MOUSE));
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
        enigma.getLightBoard().display();
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

    private void processYankArmature() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(enigma.getArmature().toString()), null);
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

    public static class ScramblerCache {
        private final EntryWheel entryWheel;
        private final Rotor[] rotors;
        private final Reflector reflector;
        private final PlugBoard plugBoard;

        public ScramblerCache(Enigma enigma) {
            this.entryWheel = enigma.getArmature().getEntryWheel();
            this.rotors = enigma.getArmature().getRotors();
            this.reflector = enigma.getArmature().getReflector();
            this.plugBoard = enigma.getPlugBoard();
        }

        public EntryWheel getEntryWheel() {
            return entryWheel;
        }

        public Rotor[] getRotors() {
            return rotors;
        }

        public Reflector getReflector() {
            return reflector;
        }

        public PlugBoard getPlugBoard() {
            return plugBoard;
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

        <T extends ScramblerType<?, ?>> void setCompleter(Class<T> scramblerTypeType) {
            if (ReflectorType.class.isAssignableFrom(scramblerTypeType)) {
                setCompleter(completeFor(enigma.getConfigContainer()::getReflectorTypes));
            } else if (EntryWheelType.class.isAssignableFrom(scramblerTypeType)) {
                setCompleter(completeFor(enigma.getConfigContainer()::getEntryWheelTypes));
            } else if (RotorType.class.isAssignableFrom(scramblerTypeType)) {
                setCompleter(completeFor(enigma.getConfigContainer()::getRotorTypes));
            }
        }

        private <T extends ScramblerType<?, ?>> Completer completeFor(Supplier<List<T>> supplier) {
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
