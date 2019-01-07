package camel.enigma.io;

import camel.enigma.exception.ArmatureInitException;
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
    private Terminal terminal;
    private NonBlockingReader reader;
    private LineReader selectRotorReader;
    private final Pattern numbers = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;
    private boolean filterCompletion;
    private String newAlphabetString = null;

    KeyBoard(KeyBoardEndpoint endpoint, Processor processor, Terminal terminal) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.terminal = terminal;
        terminal.handle(Terminal.Signal.INT, signal -> processQuit());
        this.selectRotorReader = initRotorReader(terminal);
        this.reader = terminal.reader();
        this.bindingReader = new BindingReader(reader);
        keyMap = getKeyMap(terminal);
    }

    private LineReader initRotorReader(Terminal terminal) {
        LineReader result = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new RotorsCompleter())
                .build();
        result.setOpt(LineReader.Option.AUTO_MENU);
        result.setOpt(LineReader.Option.ERASE_LINE_ON_FINISH);
        result.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        result.setOpt(LineReader.Option.COMPLETE_IN_WORD);
        result.setOpt(LineReader.Option.DISABLE_HIGHLIGHTER);
        result.setVariable(LineReader.DISABLE_HISTORY, true);
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
            readFromStream();
        } catch (Exception e) {
            getExceptionHandler().handleException(e);
        }
    }

    public enum Op {
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
                        processSelectRotors();
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

    private void processSelectRotors() {
        int rotorNo;
        RotorType[] newRotorTypes;
        selectRotorReader.setVariable(LineReader.DISABLE_COMPLETION, true);
        try {
            rotorNo = promptForRotorNo();
        } catch (UserInterruptException e) {
            return;
        }
        selectRotorReader.setVariable(LineReader.DISABLE_COMPLETION, false);
        try {
            newRotorTypes = promptForRotorTypes(rotorNo);
        } catch (UserInterruptException e) {
            return;
        }
        try {
            endpoint.getArmature().setRotors(newRotorTypes);
            endpoint.switchAlphabet(newRotorTypes[0].getAlphabetString());
        } catch (ArmatureInitException e) {
            selectRotorReader.printAbove("Can't change rotors: " + e.getMessage());
        }
    }

    private int promptForRotorNo() {
        boolean choose = true;
        String prompt = "Enter number of rotors: ";
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

    private RotorType[] promptForRotorTypes(int rotorNo) {
        RotorType[] newRotorTypes = new RotorType[rotorNo];
        String promptFormat = "Enter a type for rotor %d: ";
        String notATypeString = "Not a valid rotorType";
        String oldAlphabetString = endpoint.getAlphabetString();
        String prompt;
        int i = 0;
        filterCompletion = false;
        while (i < rotorNo) {
            prompt = String.format(promptFormat, i);
            String tInput = selectRotorReader.readLine(prompt).trim();
            Optional<RotorType> rotorType = endpoint.getConfigContainer().getRotorTypes().stream()
                    .filter(rotorType1 -> rotorType1.getName().equals(tInput)).findAny();
            if (rotorType.isPresent()) {
                RotorType type = rotorType.get();
                String typeAlphabetString = type.getAlphabetString();
                if (i == 0) {
                    newAlphabetString = typeAlphabetString;
                    filterCompletion = true;
                } else if (newAlphabetString != null && !newAlphabetString.equals(typeAlphabetString)) {
                    selectRotorReader.printAbove("Incompatible alphabetString.");
                    continue;
                }
                newRotorTypes[i] = type;
                i++;
            } else {
                selectRotorReader.printAbove(notATypeString);
            }
        }
        return newRotorTypes;
    }

    private KeyMap<Op> getKeyMap(Terminal terminal) {
        KeyMap<Op> result = new KeyMap<>();
        Set<String> alphabetChars = getUpperAndLower(Scrambler.DEFAULT_ALPHABET);
        bind(result, Op.ENTER_CHAR, alphabetChars);
        bind(result, Op.RESET_OFFSETS, ctrl('R'));
        bind(result, Op.DETAIL_MODE_TOGGLE, ctrl('B'));
        bind(result, Op.SELECT_ROTOR, ctrl('I'));
        bind(result, Op.UP, key(terminal, InfoCmp.Capability.key_up));
        bind(result, Op.DOWN, key(terminal, InfoCmp.Capability.key_down));
        bind(result, Op.LEFT, key(terminal, InfoCmp.Capability.key_left));
        bind(result, Op.RIGHT, key(terminal, InfoCmp.Capability.key_right));
        return result;
    }

    private void bind(KeyMap<Op> map, Op op, Iterable<? extends CharSequence> keySeqs) {
        map.bind(op, keySeqs);
    }

    private void bind(KeyMap<Op> map, Op op, CharSequence... keySeqs) {
        map.bind(op, keySeqs);
    }

    private static Set<String> getUpperAndLower(char[] upperCaseAlphaber) {
        Set<String> upperCaseChars = IntStream.range(0, upperCaseAlphaber.length)
                .mapToObj(value -> String.valueOf(upperCaseAlphaber[value]))
                .collect(Collectors.toSet());
        Set<String> alphabetChars = new HashSet<>(upperCaseChars);
        upperCaseChars.forEach(s -> alphabetChars.add(s.toLowerCase()));
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

    private class RotorsCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<RotorType> rotorTypes = endpoint.getConfigContainer().getRotorTypes();
            candidates.addAll(rotorTypes.stream()
                                      // TODO filter by armature alphabet
                                      .filter(rotorType -> !filterCompletion || (newAlphabetString != null &&
                                              newAlphabetString.equals(rotorType.getAlphabetString())))
                                      .map(rotorType -> new Candidate(rotorType.getName()))
                                      .collect(Collectors.toList()));
        }
    }
}
