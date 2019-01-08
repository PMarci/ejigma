package camel.enigma.io;

import camel.enigma.exception.ArmatureInitException;
import camel.enigma.model.type.EntryWheelType;
import camel.enigma.model.type.RotorType;
import camel.enigma.model.type.ScramblerType;
import camel.enigma.util.Properties;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

public class KeyBoard extends DefaultConsumer implements Runnable {

    private final String FILTER_COMPLETION = "filterCompletion";
    private KeyBoardEndpoint endpoint;
    private ExecutorService executor;
    private Terminal terminal;
    private LineReader selectionReader;
    private final Pattern numbers = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;
    //    private String oldAlphabetString;
    private String newAlphabetString = null;
    private SelectCompleter selectCompleter;

    KeyBoard(KeyBoardEndpoint endpoint, Processor processor, Terminal terminal) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.terminal = terminal;
//        this.oldAlphabetString = endpoint.getAlphabetString();
        terminal.handle(Terminal.Signal.INT, signal -> processQuit());
        this.selectionReader = initSelectionReader(terminal);
        this.bindingReader = new BindingReader(terminal.reader());
        keyMap = getKeyMap(terminal);
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

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        executor = endpoint.getCamelContext()
            .getExecutorServiceManager()
            .newSingleThreadExecutor(this, "keyBoardThread");
        executor.execute(this);

    }

    @Override
    protected void doStop() throws Exception {
        terminal.puts(InfoCmp.Capability.newline);
        log.info("Stopping Consumer...");
        terminal.close();

        if (executor != null) {
            endpoint.getCamelContext().getExecutorServiceManager().shutdownGraceful(executor, 300);
            executor = null;
        }

        super.doStop();
        log.info("Stopping CamelContext...");
        endpoint.getCamelContext().stop();
    }

    @Override
    public void run() {
        try {
            readFromStream();
        } catch (Exception e) {
            getExceptionHandler().handleException(e);
        }
    }

    public enum Op {
        ENTER_CHAR,
        SELECT_ENTRY,
        SELECT_ROTOR,
        SELECT_REFLECTOR,
        RESET_OFFSETS,
        DETAIL_MODE_TOGGLE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private void readFromStream() throws Exception {
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
                    processSelectEntry();
                    break;
                case SELECT_ROTOR:
                    processSelectRotors(true, true);
                    break;
                case SELECT_REFLECTOR:
                    processSelectReflector();
                    break;
                case RESET_OFFSETS:
                    processResetOffsets();
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

    private void processSelectEntry() {
        ScramblerSelectResponse<EntryWheelType> newEntryWheelTypeResponse;
        try {
            newEntryWheelTypeResponse = promptForEntryType();
        } catch (UserInterruptException e) {
            return;
        }
        EntryWheelType newType = newEntryWheelTypeResponse.getScramblerType();
        if (newEntryWheelTypeResponse.isReselect()) {
            endpoint.getArmature().setEntryWheel(newType, true);
            processSelectRotors(false, false, newType);
            // TODO reflector too
        } else {
            endpoint.getArmature().setEntryWheel(newType, false);
        }
    }

    private ScramblerSelectResponse<EntryWheelType> promptForEntryType() {
        boolean choose = true;
        boolean choose2;
        EntryWheelType newEntryWheelType;
        ScramblerSelectResponse<EntryWheelType> response = null;
        String notATypeString = "Not a valid entryWheelType";
        String prompt = "Enter a type for the entryWheel: ";
        String switchPrompt = "EntryWheel doesn't fit other scramblers in armature, change them as well? (y/n): ";
        String denyString = "Not setting entryWheel...";
        selectCompleter.setCompleter(new EntryWheelCompleter());
        while (choose) {
            choose2 = true;
            selectionReader.setVariable(FILTER_COMPLETION, false);
            String tInput = selectionReader.readLine(prompt).trim();
            Optional<EntryWheelType> optionalEntryWheelType = endpoint.getConfigContainer().getEntryWheelTypes().stream()
                .filter(eWType -> eWType.getName().equals(tInput)).findAny();
            if (optionalEntryWheelType.isPresent()) {
                newEntryWheelType = optionalEntryWheelType.get();
                this.newAlphabetString = newEntryWheelType.getAlphabetString();
                selectionReader.setVariable(FILTER_COMPLETION, false);
                boolean fits = endpoint.getArmature().validateWithCurrent(newEntryWheelType);
                if (!fits) {
                    selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
                    while (choose2) {
                        String yn = selectionReader.readLine(switchPrompt).trim();
                        if ("y".equals(yn) || "Y".equals(yn)) {
                            response = new ScramblerSelectResponse<>(newEntryWheelType, true);
                            choose = false;
                            choose2 = false;
                        } else if ("n".equals(yn) || "N".equals(yn)) {
                            selectionReader.printAbove(denyString);
                            choose2 = false;
                        }
                    }
                    selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
                } else {
                    response = new ScramblerSelectResponse<>(newEntryWheelType, false);
                    choose = false;
                    choose2 = false;
                }
            } else {
                selectionReader.printAbove(notATypeString);
            }
        }
        return response;
    }

    private void processSelectRotors(boolean autoEntryWheel, boolean autoRandomReflector) {
        processSelectRotors(autoEntryWheel, autoRandomReflector, null);
    }

    private void processSelectRotors(boolean autoEntryWheel, boolean autoRandomReflector, EntryWheelType prevType) {
        int rotorNo;
        RotorType[] newRotorTypes;
        boolean completionWarranted = !(autoEntryWheel && autoRandomReflector);
        selectionReader.setVariable(LineReader.DISABLE_COMPLETION, true);
        try {
            rotorNo = promptForRotorNo();
        } catch (UserInterruptException e) {
            return;
        }
        selectionReader.setVariable(LineReader.DISABLE_COMPLETION, false);
        selectCompleter.setCompleter(new RotorsCompleter());
        try {
            newRotorTypes = promptForRotorTypes(rotorNo, completionWarranted);
        } catch (UserInterruptException e) {
            return;
        }
        if (newRotorTypes.length > 0) {
            try {
                String vNewAlphabetString = newRotorTypes[0].getAlphabetString();
//                char[] newAlphabet = vNewAlphabetString.toCharArray();
//                char[] oldAlphabet = oldAlphabetString.toCharArray();
                endpoint.getArmature().setRotors(newRotorTypes, autoEntryWheel, autoRandomReflector);
                endpoint.switchAlphabet(vNewAlphabetString);
//                Set<String> oldUpperAndLower = getUpperAndLower(oldAlphabet, Locale.ROOT);
//                Set<String> upperAndLower = getUpperAndLower(newAlphabet, Locale.ROOT);
//                unbind(keyMap, oldUpperAndLower);
//                bind(keyMap, Op.ENTER_CHAR, upperAndLower);
                keyMap.setUnicode(Op.ENTER_CHAR);
            } catch (ArmatureInitException e) {
                if (prevType != null) {
                    endpoint.getArmature().setEntryWheel(prevType, true);
                }
                selectionReader.printAbove("Can't change rotors: " + e.getMessage());
            }
        }
        endpoint.getLightBoard().redisplay();
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

    private RotorType[] promptForRotorTypes(int rotorNo, boolean startFiltered) {
        RotorType[] newRotorTypes = new RotorType[rotorNo];
        String promptFormat = "Enter a type for rotor %d: ";
        String notATypeString = "Not a valid rotorType";
        String prompt;
        int i = 0;
        if (startFiltered) {
            selectionReader.setVariable(FILTER_COMPLETION, true);
        }
        while (i < rotorNo) {
            prompt = String.format(promptFormat, i);
            String tInput = selectionReader.readLine(prompt).trim();
            Optional<RotorType> rotorType = endpoint.getConfigContainer().getRotorTypes().stream()
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
        return newRotorTypes;
    }

    private void processSelectReflector() {

    }

    private KeyMap<Op> getKeyMap(Terminal terminal) {
        KeyMap<Op> result = new KeyMap<>();
        Set<String> alphabetChars = getUpperAndLower(endpoint.getAlphabetString().toCharArray(), Locale.ROOT);
        bind(result, Op.ENTER_CHAR, alphabetChars);
        result.setUnicode(Op.ENTER_CHAR);
        bind(result, Op.RESET_OFFSETS, ctrl('R'));
        bind(result, Op.DETAIL_MODE_TOGGLE, ctrl('B'));
        bind(result, Op.SELECT_ROTOR, ctrl('I'));
        bind(result, Op.SELECT_ENTRY, ctrl('E'));
        bind(result, Op.SELECT_REFLECTOR, ctrl('F'));
        bind(result, Op.UP, key(terminal, InfoCmp.Capability.key_up));
        bind(result, Op.DOWN, key(terminal, InfoCmp.Capability.key_down));
        bind(result, Op.LEFT, key(terminal, InfoCmp.Capability.key_left));
        bind(result, Op.RIGHT, key(terminal, InfoCmp.Capability.key_right));
        return result;
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

    private void processInput(char input) throws Exception {
        Exchange exchange = endpoint.createExchange(input);
        getProcessor().process(exchange);
    }

    private void processControl(Op input) {
        LightBoard lightBoard = endpoint.getLightBoard();
        if (input == Op.LEFT) {
            lightBoard.getBuffer().move(-1);
        }
        if (input == Op.RIGHT) {
            lightBoard.getBuffer().move(1);
        }
        lightBoard.redisplay();

    }

    private void processResetOffsets() throws Exception {
        Exchange exchange = endpoint.createExchange();
        exchange.setProperty(Properties.RESET_OFFSETS, true);
        getProcessor().process(exchange);
    }

    private void processDetailModeToggle() throws Exception {
        Exchange exchange = endpoint.createExchange();
        exchange.setProperty(Properties.DETAIL_MODE_TOGGLE, true);
        getProcessor().process(exchange);
    }

    private void processQuit() {
        try {
            stop();
        } catch (Exception e) {
            log.error("An exception occurred while stopping the consumer: ", e);
        }
    }

    public class ScramblerSelectResponse<T extends ScramblerType> {

        private T scramblerType;
        private boolean reselect;

        public ScramblerSelectResponse(T scramblerType, boolean reselect) {
            this.scramblerType = scramblerType;
            this.reselect = reselect;
        }

        public T getScramblerType() {
            return scramblerType;
        }

        public boolean isReselect() {
            return reselect;
        }
    }

    private class SelectCompleter implements Completer {

        Completer completer;

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            completer.complete(reader, line, candidates);
        }

        public void setCompleter(Completer completer) {
            this.completer = completer;
        }
    }

    private class RotorsCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<RotorType> rotorTypes = endpoint.getConfigContainer().getRotorTypes();
            candidates.addAll(rotorTypes.stream()
                .filter(rotorType ->
                    !((Boolean) reader.getVariable(FILTER_COMPLETION)) || (newAlphabetString != null && newAlphabetString
                        .equals(rotorType.getAlphabetString())))
                .map(rotorType -> new Candidate(rotorType.getName()))
                .collect(Collectors.toSet()));
        }
    }

    private class EntryWheelCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<EntryWheelType> entryWheelTypes = endpoint.getConfigContainer().getEntryWheelTypes();
            candidates.addAll(entryWheelTypes.stream()
                .filter(entryWheelType ->
                    !((Boolean) reader.getVariable(FILTER_COMPLETION)) || (newAlphabetString != null && newAlphabetString
                        .equals(entryWheelType.getAlphabetString())))
                .map(rotorType -> new Candidate(rotorType.getName()))
                .collect(Collectors.toSet()));
        }
    }
}
