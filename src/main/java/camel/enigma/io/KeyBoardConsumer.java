package camel.enigma.io;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

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
        while (isRunAllowed()) {
            input = ((char) reader.read());
            processInput(input);
        }
    }

    private void exitPrompt() throws IOException {
        System.out.printf("%n");
        char input = 'x';
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
            log.info("\nReceived SIGINT via Ctrl+C, exit application? [y/n]");
                input = ((char) reader.read());
        } while (input != 'y' && input != 'Y' && input != 'n' && input != 'N');
        if (input == 'y' || input == 'Y') {
            log.info("\nExiting...");
            try {
                terminal.close();
                doStop();
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
