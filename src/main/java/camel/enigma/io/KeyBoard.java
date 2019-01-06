package camel.enigma.io;

import camel.enigma.model.Scrambler;
import camel.enigma.model.type.RotorType;
import camel.enigma.util.Properties;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

public class KeyBoard extends DefaultConsumer implements Runnable {

    private KeyBoardEndpoint endpoint;
    private ExecutorService executor;
    private boolean debugMode;
    private Terminal terminal;
    private NonBlockingReader reader;
    private LineReader selectRotorReader;
    private final Pattern numbers = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;

    KeyBoard(KeyBoardEndpoint endpoint, Processor processor, boolean debugMode, Terminal terminal) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.debugMode = debugMode;
        this.terminal = terminal;
        terminal.handle(Terminal.Signal.INT, signal -> processQuit());
        selectRotorReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new RotorsCompleter())
                .build();
        selectRotorReader.setOpt(LineReader.Option.AUTO_MENU);
        selectRotorReader.setOpt(LineReader.Option.ERASE_LINE_ON_FINISH);
        selectRotorReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        selectRotorReader.setOpt(LineReader.Option.COMPLETE_IN_WORD);
        selectRotorReader.setOpt(LineReader.Option.DISABLE_HIGHLIGHTER);
        selectRotorReader.setVariable(LineReader.DISABLE_HISTORY, true);
        this.reader = terminal.reader();
        this.bindingReader = new BindingReader(reader);
        keyMap = new KeyMap<>();
        Set<String> upperCaseChars = IntStream.range(0, Scrambler.DEFAULT_ALPHABET.length)
                .mapToObj(value -> String.valueOf(Scrambler.DEFAULT_ALPHABET[value]))
                .collect(Collectors.toSet());
        Set<String> alphabetChars = new HashSet<>(upperCaseChars);
        upperCaseChars.forEach(s -> alphabetChars.add(s.toLowerCase()));
        bind(keyMap, Op.ENTER_CHAR, alphabetChars);
        bind(keyMap, Op.RESET_OFFSETS, ctrl('R'));
        bind(keyMap, Op.DETAIL_MODE_TOGGLE, ctrl('B'));
        bind(keyMap, Op.SELECT_ROTOR, ctrl('I'));
        bind(keyMap, Op.UP, key(terminal, InfoCmp.Capability.key_up));
        bind(keyMap, Op.DOWN, key(terminal, InfoCmp.Capability.key_down));
        bind(keyMap, Op.LEFT, key(terminal, InfoCmp.Capability.key_left));
        bind(keyMap, Op.RIGHT, key(terminal, InfoCmp.Capability.key_right));
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
        terminal.close();

        if (executor != null) {
            endpoint.getCamelContext().getExecutorServiceManager().shutdownGraceful(executor, 500);
            executor = null;
        }

        super.doStop();
        log.info("Stopping CamelContext...");
        endpoint.getCamelContext().stop();
    }

    @Override
    public void run() {
        try {
            if (!debugMode) {
                readFromStream();
            } else {
                readFromStreamDebug();
            }
        } catch (Exception e) {
            getExceptionHandler().handleException(e);
        }
    }

    public enum Op {
        //        QUIT,
        ENTER_CHAR,
        SELECT_ROTOR,
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
                    case SELECT_ROTOR:
                        int rotorNo;
                        RotorType[] newRotorTypes;
                        selectRotorReader.setVariable(LineReader.DISABLE_COMPLETION, true);
                        try {
                            rotorNo = promptForRotorNo();
                        } catch (UserInterruptException e) {
                            break;
                        }
                        selectRotorReader.setVariable(LineReader.DISABLE_COMPLETION, false);
                        try {
                            newRotorTypes = promptForTypes(rotorNo);
                        } catch (UserInterruptException e) {
                            break;
                        }
                        endpoint.getArmature().setRotors(newRotorTypes);
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

    private int promptForRotorNo() {
        boolean choose = true;
        String prompt = "Enter number of Rotors: ";
        String nanString = "That's not a number.";
        int rotorNo = 0;
        while (choose) {
            String nInput;
            nInput = selectRotorReader.readLine(prompt);

            Matcher numberMatcher = numbers.matcher(nInput);
            if (numberMatcher.matches()) {
                try {
                    rotorNo = Integer.valueOf(numberMatcher.group(1));
                } catch (NumberFormatException e) {
                    selectRotorReader.printAbove(nanString);
                    continue;
                }
                choose = false;
            } else {
                selectRotorReader.printAbove(nanString);
            }
        }
        return rotorNo;
    }

    private RotorType[] promptForTypes(int rotorNo) {
        RotorType[] newRotorTypes = new RotorType[rotorNo];
        String promptFormat = "Enter a type for rotor %d: ";
        String notATypestring = "Not a valid rotorType";
        String prompt;
        int i = 0;
        while (i < rotorNo) {
            prompt = String.format(promptFormat, i);
            String tInput;
            tInput = selectRotorReader.readLine(prompt).trim();
            Optional<RotorType> rotorType = endpoint.getConfigContainer().getRotorTypes().stream()
                    .filter(rotorType1 -> rotorType1.getName().equals(tInput)).findAny();
            if (rotorType.isPresent()) {
                newRotorTypes[i] = rotorType.get();
                i++;
            } else {
                selectRotorReader.printAbove(notATypestring);
            }
        }
        return newRotorTypes;
    }

    private void processControl(Op input) {
        // TODO
    }

    private void bind(KeyMap<Op> map, Op op, Iterable<? extends CharSequence> keySeqs) {
        map.bind(op, keySeqs);
    }

    private void bind(KeyMap<Op> map, Op op, CharSequence... keySeqs) {
        map.bind(op, keySeqs);
    }

    private void readFromStreamDebug() throws Exception {
        char input;
        while (isRunAllowed()) {
            input = ((char) reader.read());
            if (input == 'c') {
                terminal.raise(Terminal.Signal.INT);
            }
            // skipping the enter keypress required by IDE
            reader.read();
            processInput(input);
        }
    }

    private void processInput(char input) throws Exception {
        Exchange exchange = endpoint.createExchange(input);
        getProcessor().process(exchange);
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

    private class RotorsCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<RotorType> rotorTypes = endpoint.getConfigContainer().getRotorTypes();
            candidates.addAll(rotorTypes
                                      .stream()
                                      .map(rotorType -> new Candidate(rotorType.getName()))
                                      .collect(Collectors.toList()));
        }
    }
}
