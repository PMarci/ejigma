package camel.enigma.io;

import camel.enigma.util.RawConsoleInput;
import camel.enigma.util.Util;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

import java.util.concurrent.ExecutorService;

public class KeyBoardConsumer extends DefaultConsumer implements Runnable {

    private KeyBoardEndpoint endpoint;
    private ExecutorService executor;
    private boolean debugMode;
    private final char[] alphabet;

    public KeyBoardConsumer(KeyBoardEndpoint endpoint, Processor processor, boolean debugMode, char[] alphabet) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.debugMode = debugMode;
        this.alphabet = alphabet;
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
        } catch (InterruptedException ignored) {
            //ignoring
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            getExceptionHandler().handleException(e);
        }
    }

    private void readFromStream() throws Exception {
        char input;
        while (isRunAllowed()) {
            input = ((char) RawConsoleInput.read(true));
            if (input == 3) {
                // TODO look at what the Unix version does
                RawConsoleInput.resetConsoleMode();
                log.info("\nReceived SIGINT via Ctrl+C, stopping console listening...");
                break;
            }
            processInput(input);
        }
    }

    private void readFromStreamDebug() throws Exception {
        char input;
        while (isRunAllowed()) {
            input = ((char) RawConsoleInput.read(true));
            // skipping the enter keypress required by IDE
            RawConsoleInput.read(true);
            processInput(input);
        }
    }

    private void processInput(char input) throws Exception {
        if (!Util.containsChar(alphabet, input)) {
            char upperCase = Character.toUpperCase(input);
            if (Util.containsChar(alphabet, upperCase)) {
                input = upperCase;
            }
        }
        if (Util.containsChar(alphabet, input)) {
            Exchange exchange = endpoint.createExchange(input);
            getProcessor().process(exchange);
        }
    }

    public char[] getAlphabet() {
        return alphabet;
    }
}
