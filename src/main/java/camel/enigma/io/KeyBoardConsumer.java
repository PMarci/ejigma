package camel.enigma.io;

import camel.enigma.util.RawConsoleInput;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

import java.util.concurrent.ExecutorService;

public class KeyBoardConsumer extends DefaultConsumer implements Runnable {

    private KeyBoardEndpoint endpoint;
    private ExecutorService executor;

    public KeyBoardConsumer(KeyBoardEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
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
            readFromStream();
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
            Exchange exchange = endpoint.createExchange(input);
            getProcessor().process(exchange);
        }
    }
}
