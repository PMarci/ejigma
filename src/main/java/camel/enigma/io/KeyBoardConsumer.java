package camel.enigma.io;

import camel.enigma.model.Scrambler;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Reference;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeyBoardConsumer extends DefaultConsumer implements Runnable {

    private KeyBoardEndpoint endpoint;
    private ExecutorService executor;
    private boolean debugMode;
    private Terminal terminal;
    private NonBlockingReader reader;

    KeyBoardConsumer(KeyBoardEndpoint endpoint, Processor processor, boolean debugMode) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.debugMode = debugMode;
        try {
            initTerm();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTerm() throws IOException {
        terminal = TerminalBuilder.builder()
                .system(true)
                .encoding(StandardCharsets.UTF_8)
                .nativeSignals(true)
                .signalHandler(signal -> {
                    if (signal == Terminal.Signal.INT) {
                        terminal.pause();
                        try {
                            exitPrompt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .jansi(true)
                .build();
        terminal.enterRawMode();
        reader = terminal.reader();
        LightBoard.setTerminal(terminal);
//        LightBoard.advanceForDiff(2);
    }

    private void initSecondTerm() throws IOException {
        terminal = TerminalBuilder.builder()
                .system(true)
                .encoding(StandardCharsets.UTF_8)
                .signalHandler(Terminal.SignalHandler.SIG_IGN)
                .jansi(true)
                .build();
        terminal.enterRawMode();
        reader = terminal.reader();
        LightBoard.setTerminal(terminal);
//        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
//        lineReader.setKeyMap()
//        lineReader.readLine()
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        executor = endpoint.getCamelContext()
                .getExecutorServiceManager()
                .newSingleThreadExecutor(this, endpoint.getEndpointUri());
        executor.execute(this);

    }

    @Override
    protected void doStop() throws Exception {
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

    private void readFromStream() throws Exception {
        char input;
        BindingReader bindingReader = new BindingReader(reader);
        // TODO implement LineReader
        KeyMap<Binding> keyMap = new KeyMap<>();
        bind(keyMap, "enter-char", IntStream.range(0, Scrambler.DEFAULT_ALPHABET.length)
                .mapToObj(value -> String.valueOf(Scrambler.DEFAULT_ALPHABET[value]))
                .collect(Collectors.toList()));
        while (isRunAllowed()) {
            input = ((char) reader.read());
//            input = ((char) bindingReader.readCharacter());
            processInput(input);
        }
    }

    private void bind(KeyMap<Binding> map, String widget, Iterable<? extends CharSequence> keySeqs) {
        map.bind(new Reference(widget), keySeqs);
    }

    private void exitPrompt() throws IOException {
        System.out.printf("%n");
        char input;
        try {
            doStop();
            terminal.close();
            initSecondTerm();
            // this interrupts the reader
            reader.read(1);
        } catch (InterruptedIOException ignored) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        do {
            log.info("Received SIGINT via Ctrl+C, exit application? [y/n]");
            input = ((char) reader.read());
        } while (input != 'y' && input != 'Y' && input != 'n' && input != 'N');
        if (input == 'y' || input == 'Y') {
            log.info("\nExiting...");
            try {
                terminal.close();
                doStop();
                this.getEndpoint().getCamelContext().stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            log.info("\nResuming...");
            try {
                // the key to making a prompt like this work seems to be
                // interrupting the waiting readers thread in the main loop
                terminal.close();
                initTerm();
                doStart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
}
