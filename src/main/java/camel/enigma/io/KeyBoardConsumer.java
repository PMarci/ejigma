package camel.enigma.io;

import camel.enigma.util.RawConsoleInput;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

import java.util.concurrent.ExecutorService;

public class KeyBoardConsumer extends DefaultConsumer implements Runnable {

    private KeyBoardEndpoint endpoint;
    private ExecutorService executor;
    private boolean debugMode;

    public KeyBoardConsumer(KeyBoardEndpoint endpoint, Processor processor, boolean debugMode) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.debugMode = debugMode;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        executor = endpoint.getCamelContext().getExecutorServiceManager().newSingleThreadExecutor(this, endpoint.getEndpointUri());
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
        } catch (Exception e) {
            getExceptionHandler().handleException(e);
        }
    }

    private void readFromStream() throws Exception {
        char input;
        while (isRunAllowed()) {
            input = ((char) RawConsoleInput.read(true));
            if (input == 0x3) {
                log.info("Recieved SIGINT via Ctrl+C, stopping console listening...");
                // TODO look at Unix version does
                RawConsoleInput.resetConsoleMode();
                break;
            }
            // TODO debuggable solution
            if (input == '\r' || input == '\n') {
                input = 'A';
            }
            Exchange exchange = endpoint.createExchange(input);
            getProcessor().process(exchange);
        }
    }

    private void readFromStreamDebug() throws Exception {
        char input;
        while (isRunAllowed()) {
            input = ((char) RawConsoleInput.read(true));
            // skipping the enter keypress required by IDE
            RawConsoleInput.read(true);
            Exchange exchange = endpoint.createExchange(input);
            getProcessor().process(exchange);
        }
    }
}
