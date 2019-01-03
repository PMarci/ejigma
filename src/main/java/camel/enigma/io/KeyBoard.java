package camel.enigma.io;

import camel.enigma.model.Scrambler;
import camel.enigma.util.Properties;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import java.io.InterruptedIOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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
    private final BindingReader bindingReader;
    private final KeyMap<Op> keyMap;

    KeyBoard(KeyBoardEndpoint endpoint, Processor processor, boolean debugMode, Terminal terminal) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.debugMode = debugMode;
        this.terminal = terminal;
        terminal.handle(Terminal.Signal.INT, signal -> processQuit());
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
//        bind(keyMap, Op.QUIT, ctrl('C'));
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
            endpoint.getCamelContext().getExecutorServiceManager().shutdownNow(executor);
            executor = null;
        }

        super.doStop();
    }

    @Override
    public void run() {
        try {
            if (!debugMode) {
                readFromStream();
            } else {
                readFromStreamDebug();
            }
        } catch (InterruptedException | InterruptedIOException ignored) {
            //ignoring
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            getExceptionHandler().handleException(e);
        }
    }

    private enum Op {
        //        QUIT,
        ENTER_CHAR,
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
        // TODO implement LineReader
        while (isRunAllowed()) {
            input = bindingReader.readBinding(keyMap, null, false);
            if (input != null) {
                switch (input) {
                    case ENTER_CHAR:
                        inputChar = bindingReader.getLastBinding().charAt(0);
                        processInput(inputChar);
                        break;
                    case RESET_OFFSETS:
                        processResetOffsets();
                        break;
                    case DETAIL_MODE_TOGGLE:
                        processDetailModeToggle();
                        break;
                    case UP:
                        System.out.println("UP");
                        break;
                    case DOWN:
                        System.out.println("DOWN");
                        break;
                    case LEFT:
                        System.out.println("LEFT");
                        break;
                    case RIGHT:
                        System.out.println("RIGHT");
                        break;
//                    case QUIT:
//                        terminal.close();
//                        getEndpoint().getCamelContext().stop();
//                        break;
                }
            }
        }
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
            System.out.println("bang");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
