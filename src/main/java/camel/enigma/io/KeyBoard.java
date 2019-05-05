package camel.enigma.io;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.model.Armature;
import camel.enigma.model.Scrambler;
import camel.enigma.model.type.*;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

@Component
public class KeyBoard implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(KeyBoard.class);
    private static final String FILTER_COMPLETION = "filterCompletion";
    private static final Pattern typeSuffixPattern = Pattern.compile("^(.*)Type$");

    private Terminal terminal;
    private LineReader selectionReader;
    private final Pattern numbers = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;

    private String newAlphabetString = null;
    private SelectCompleter selectCompleter;

    private ExecutorService executor;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final Armature armature;
    private final LightBoard lightBoard;

    private String alphabetString;
    private char[] alphabet;

    private final ConfigContainer configContainer;

    private static final String SWITCH_PROMPT = ", change them as well? (y/n): ";


    @Autowired
    KeyBoard(Terminal terminal,
             ConfigContainer configContainer,
             Armature armature,
             LightBoard lightBoard) {
        setAlphabet(Scrambler.DEFAULT_ALPHABET_STRING);
        this.terminal = terminal;
        this.lightBoard = lightBoard;
        terminal.handle(Terminal.Signal.INT, signal -> processQuit());
        this.selectionReader = initSelectionReader(terminal);
        this.bindingReader = new BindingReader(terminal.reader());
        this.keyMap = initKeyMap(new KeyMap<>());

        this.armature = armature;
        this.configContainer = configContainer;
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
        terminal.puts(InfoCmp.Capability.newline);
        logger.info("Stopping...");
        terminal.close();

        if (executor != null) {
//            endpoint.getCamelContext().getExecutorServiceManager().shutdownGraceful(executor, 300);
//            TODO maybe implement awaitTermination
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
            logger.error("big oof", e);
        }
    }

    public enum Op {
        ENTER_CHAR,
        SELECT_ENTRY,
        SELECT_ROTOR,
        SELECT_REFLECTOR,
        RESET_OFFSETS,
        CLEAR_BUFFER,
        DETAIL_MODE_TOGGLE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private void readFromStream() {
        Op input;
        char inputChar;
        while (isRunAllowed()) {
            input = bindingReader.readBinding(keyMap, null, false);
            if (input != null) {
                switch (input) {
                    case ENTER_CHAR:
                        inputChar = bindingReader.getLastBinding().charAt(0);
                        processInput(inputChar);
                        break;
                    case SELECT_ENTRY:
                        processSelectEntryIgnoreInterrupt();
                        break;
                    case SELECT_ROTOR:
                        processSelectRotorsIgnoreInterrupt();
                        break;
                    case SELECT_REFLECTOR:
                        processSelectReflectorIgnoreInterrupt();
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
        }
    }

    private void processSelectEntry() {
        ScramblerSelectResponse<EntryWheelType> newEntryWheelTypeResponse;
        newEntryWheelTypeResponse = promptForScramblerType(EntryWheelType.class);
        EntryWheelType newType = newEntryWheelTypeResponse.getScramblerType();
        String vNewAlphabetString = newType.getAlphabetString();
        if (newEntryWheelTypeResponse.isReselect()) {
            armature.forceSetEntryWheel(newType);
            processSelectRotors();
            processSelectReflector();
            rebindKeyMap(vNewAlphabetString);
        } else {
            try {
                armature.setEntryWheel(newType);
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
        }
    }

    private void processSelectReflector() {
        ScramblerSelectResponse<ReflectorType> newReflectorTypeResponse;
        newReflectorTypeResponse = promptForScramblerType(ReflectorType.class);
        ReflectorType newType = newReflectorTypeResponse.getScramblerType();
        String vNewAlphabetString = newType.getAlphabetString();
        if (newReflectorTypeResponse.isReselect()) {
            armature.forceSetReflector(newType);
            processSelectEntry();
            processSelectRotors();
            rebindKeyMap(vNewAlphabetString);
        } else {
            try {
                armature.setReflector(newType);
            } catch (ArmatureInitException e) {
                selectionReader.printAbove("Can't change reflector: " + e.getMessage());
            }
        }
        lightBoard.redisplay();
    }

    private <T extends ScramblerType> ScramblerSelectResponse<T> promptForScramblerType(Class<T> scramblerTypeType) {
        boolean choose = true;
        boolean choose2;
        T newScramblerType;
        ScramblerSelectResponse<T> response = null;
        String scramblerTypeTypeName = scramblerTypeType.getSimpleName();
        Matcher typeMatcher = typeSuffixPattern.matcher(scramblerTypeTypeName);
        String scramblerName = (typeMatcher.find()) ? typeMatcher.group(1) : "Scrambler";
        String notATypeString = String.format("Not a valid %s", scramblerTypeTypeName);
        String prompt = String.format("Enter a type for the %s: ", scramblerName);
        String denyString = String.format("Not setting %s...", scramblerName);
        selectCompleter.setCompleter(new ReflectorCompleter());
        while (choose) {
            choose2 = true;
            selectionReader.setVariable(FILTER_COMPLETION, false);
            String tInput = selectionReader.readLine(prompt).trim();
            Optional<T> optionalScramblerType =
                    configContainer.getScramblerTypes(scramblerTypeType).stream()
                            .filter(sType -> sType.getName().equals(tInput)).findAny();
            if (optionalScramblerType.isPresent()) {
                newScramblerType = optionalScramblerType.get();
                this.newAlphabetString = newScramblerType.getAlphabetString();
                selectionReader.setVariable(FILTER_COMPLETION, false);
                ArmatureInitException exception = null;
                try {
                    armature.validateWithCurrent(scramblerTypeType.cast(newScramblerType));
                } catch (ArmatureInitException e) {
                    exception = e;
                }
                if (exception != null) {
                    selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
                    while (choose2) {
                        String yn = selectionReader.readLine(exception.getMessage() + SWITCH_PROMPT).trim();
                        if ("y".equals(yn) || "Y".equals(yn)) {
                            response = new ScramblerSelectResponse<>(newScramblerType, true);
                            choose = false;
                            choose2 = false;
                        } else if ("n".equals(yn) || "N".equals(yn)) {
                            selectionReader.printAbove(denyString);
                            choose2 = false;
                        }
                    }
                    selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
                } else {
                    response = new ScramblerSelectResponse<>(newScramblerType, false);
                    choose = false;
                }
            } else {
                selectionReader.printAbove(notATypeString);
            }
        }
        return response;
    }

    private void processSelectRotorsIgnoreInterrupt() {
        try {
            processSelectRotors();
        } catch (UserInterruptException ignored) {
            //ignored
        }
    }

    private void processSelectRotors() {
        int rotorNo;
        ScramblerSelectResponse<RotorType> newRotorResponse;
        selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
        rotorNo = promptForRotorNo();
        selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
        selectCompleter.setCompleter(new RotorsCompleter());
        newRotorResponse = promptForRotorTypes(rotorNo);
        if (newRotorResponse != null) {
            RotorType[] newRotorTypes = newRotorResponse.getScramblerTypes();
            String vNewAlphabetString = newRotorTypes[0].getAlphabetString();
            if (newRotorResponse.isReselect()) {
                RotorType[] oldRotorTypes = armature.getRotorTypes();
                try {
                    armature.forceSetRotors(newRotorTypes);
                    if (promptForAuto("EntryWheel")) {
                        armature.setAutoEntryWheel(vNewAlphabetString);
                    } else {
                        processSelectEntry();
                    }
                    if (promptForAuto("Reflector")) {
                        armature.setAutoReflector(vNewAlphabetString);
                    } else {
                        processSelectReflector();
                    }
                    // TODO this is not enough to ensure that the above selections happened
                } catch (UserInterruptException e) {
                    armature.forceSetRotors(oldRotorTypes);
                    return;
                }
                rebindKeyMap(vNewAlphabetString);
            } else {
                try {
                    armature.setRotors(newRotorTypes);
                } catch (ArmatureInitException e) {
                    selectionReader.printAbove("Can't change rotors: " + e.getMessage());
                }
            }
        }
        lightBoard.redisplay();
    }

    private int promptForRotorNo() {
        boolean choose = true;
        String prompt = "Enter number of rotors: ";
        String nanString = "That's not a number.";
        int rotorNo = 0;
        while (choose) {
            String nInput;
            nInput = selectionReader.readLine(prompt);

            Matcher numberMatcher = numbers.matcher(nInput);
            if (numberMatcher.matches()) {
                try {
                    rotorNo = Integer.valueOf(numberMatcher.group(1));
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

    private ScramblerSelectResponse<RotorType> promptForRotorTypes(int rotorNo) {
        ScramblerSelectResponse<RotorType> response = null;
        RotorType[] newRotorTypes = new RotorType[rotorNo];
        String promptFormat = "Enter a type for rotor %d: ";
        String notATypeString = "Not a valid rotorType";
        String denyString = "Not setting rotors...";
        String prompt;
        int i = 0;
        while (i < rotorNo) {
            prompt = String.format(promptFormat, i);
            String tInput = selectionReader.readLine(prompt).trim();
            Optional<RotorType> rotorType = configContainer.getRotorTypes().stream()
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
                selectionReader.printAbove(notATypeString);
            }
        }
        ArmatureInitException exception = null;
        try {
            armature.validateWithCurrent(newRotorTypes);
        } catch (ArmatureInitException e) {
            exception = e;
        }
        boolean choose = true;
        if (exception != null) {
            selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
            while (choose) {
                String yn = selectionReader.readLine(exception.getMessage() + SWITCH_PROMPT).trim();
                if ("y".equals(yn) || "Y".equals(yn)) {
                    response = new ScramblerSelectResponse<>(newRotorTypes, true);
                    choose = false;
                } else if ("n".equals(yn) || "N".equals(yn)) {
                    selectionReader.printAbove(denyString);
                    choose = false;
                }
            }
            selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
        } else {
            response = new ScramblerSelectResponse<>(newRotorTypes, false);
        }
        return response;
    }

    private boolean promptForAuto(String autoType) {
        String switchPrompt = String.format(",%nenable autogenerated %s? (y/n)", autoType);
        boolean result = false;
        boolean choose = true;
        while (choose) {
            String yn = selectionReader.readLine(Armature.UNFIT_ROTORTYPES_MSG + switchPrompt).trim();
            if ("y".equals(yn) || "Y".equals(yn)) {
                result = true;
                choose = false;
            } else if ("n".equals(yn) || "N".equals(yn)) {
                choose = false;
            }
        }
        return result;
    }

    private KeyMap<Op> initKeyMap(KeyMap<Op> keyMap) {
        Set<String> alphabetChars = getUpperAndLower(alphabetString.toCharArray(), Locale.ROOT);
        bind(keyMap, Op.ENTER_CHAR, alphabetChars);
        keyMap.setUnicode(Op.ENTER_CHAR);
        bind(keyMap, Op.RESET_OFFSETS, ctrl('R'));
        bind(keyMap, Op.DETAIL_MODE_TOGGLE, ctrl('B'));
        bind(keyMap, Op.SELECT_ROTOR, ctrl('I'));
        bind(keyMap, Op.SELECT_ENTRY, ctrl('E'));
        bind(keyMap, Op.SELECT_REFLECTOR, ctrl('F'));
        bind(keyMap, Op.CLEAR_BUFFER, ctrl('D'));
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
            scrambleResult = new ScrambleResult((initialResult != -1) ? initialResult : 0, alphabetString, input);
        }

        ScrambleResult armatureResult = armature.handle(scrambleResult);
        lightBoard.process(armatureResult);
    }

    private void processControl(Op input) {
        if (input == Op.LEFT) {
            lightBoard.getBuffer().move(-1);
        }
        if (input == Op.RIGHT) {
            lightBoard.getBuffer().move(1);
        }
        lightBoard.redisplay();

    }

    private void processResetOffsets() {
        terminal.puts(InfoCmp.Capability.newline);
        logger.info("\nReceived Ctrl+R, resetting offsets...");
        armature.resetOffsets();
    }

    private void processClearBuffer() {
        lightBoard.clearBuffer();
    }

    private void processDetailModeToggle() {
        terminal.puts(InfoCmp.Capability.scroll_forward);
        terminal.puts(InfoCmp.Capability.cursor_down);
        terminal.puts(InfoCmp.Capability.clear_screen);
        lightBoard.toggleDetailMode();
    }

    private void processQuit() {
        try {
//            TODO maybe take some functionality from camel lifecycle management
            doStop();
        } catch (Exception e) {
            logger.error("An exception occurred while stopping: ", e);
        }
    }

    private boolean isRunAllowed() {
        return !shutdown.get() && !shuttingDown.get();
    }

    public void setAlphabet(String alphabetString) {
        this.alphabetString = alphabetString;
        this.alphabet = alphabetString.toCharArray();
    }

    public class ScramblerSelectResponse<T extends ScramblerType> {

        private T scramblerType;
        private T[] scramblerTypes;
        private boolean reselect;

        ScramblerSelectResponse(T scramblerType, boolean reselect) {
            this.scramblerType = scramblerType;
            this.reselect = reselect;
        }

        ScramblerSelectResponse(T[] scramblerTypes, boolean reselect) {
            this.scramblerTypes = scramblerTypes;
            this.reselect = reselect;
        }

        boolean isMulti() {
            return (null == scramblerType) && (null != scramblerTypes);
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

    private class SelectCompleter implements Completer {

        Completer completer;

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            completer.complete(reader, line, candidates);
        }

        void setCompleter(Completer completer) {
            this.completer = completer;
        }
    }

    private class RotorsCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<RotorType> rotorTypes = configContainer.getRotorTypes();
            candidates.addAll(rotorTypes.stream()
                                      .filter(rotorType ->
                                                      !((Boolean) reader.getVariable(FILTER_COMPLETION)) ||
                                                              (newAlphabetString != null && newAlphabetString
                                                                      .equals(rotorType.getAlphabetString())))
                                      .map(rotorType -> new Candidate(rotorType.getName()))
                                      .collect(Collectors.toSet()));
        }
    }

    private class EntryWheelCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<EntryWheelType> entryWheelTypes = configContainer.getEntryWheelTypes();
            candidates.addAll(entryWheelTypes.stream()
                                      .filter(entryWheelType ->
                                                      !((Boolean) reader.getVariable(FILTER_COMPLETION)) ||
                                                              (newAlphabetString != null && newAlphabetString
                                                                      .equals(entryWheelType.getAlphabetString())))
                                      .map(entryWheelType -> new Candidate(entryWheelType.getName()))
                                      .collect(Collectors.toSet()));
        }
    }

    private class ReflectorCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<ReflectorType> reflectorTypes = configContainer.getReflectorTypes();
            candidates.addAll(reflectorTypes.stream()
                                      .filter(reflectorType ->
                                                      !((Boolean) reader.getVariable(FILTER_COMPLETION)) ||
                                                              (newAlphabetString != null && newAlphabetString
                                                                      .equals(reflectorType.getAlphabetString())))
                                      .map(reflectorType -> new Candidate(reflectorType.getName()))
                                      .collect(Collectors.toSet()));
        }
    }
}
