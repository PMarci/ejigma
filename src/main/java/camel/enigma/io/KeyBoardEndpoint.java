package camel.enigma.io;

import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

@UriEndpoint(firstVersion = "1.3.0", scheme = "keyboard", title = "KeyBoardEndpoint", syntax = "keyboard", consumerClass = KeyBoardConsumer.class, consumerOnly = true, label = "system")
public class KeyBoardEndpoint extends DefaultEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(KeyBoardEndpoint.class);

    static final String DEFAULT_ALPHABET_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    protected static final char[] DEFAULT_ALPHABET = DEFAULT_ALPHABET_STRING.toCharArray();

    private final char[] alphabet;
    private Charset charset;

    @UriParam
    private String encoding;
    @UriParam(label = "consumer", defaultValue = "false")
    private boolean debugMode = false;

    public KeyBoardEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
        alphabet = DEFAULT_ALPHABET;
    }

    protected Exchange createExchange(Object body) {
        Exchange exchange = createExchange();
        exchange.getIn().setBody(body);
        return exchange;
    }

    @Override
    public Producer createProducer() throws Exception {
        return null;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new KeyBoardConsumer(this, processor, debugMode, alphabet);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        charset = loadCharset();
    }

    private Charset loadCharset() {
        if (encoding == null) {
            encoding = Charset.defaultCharset().name();
            LOG.debug("No encoding parameter using default charset: {}", encoding);
        }
        if (!Charset.isSupported(encoding)) {
            throw new IllegalArgumentException("The encoding: " + encoding + " is not supported");
        }

        return Charset.forName(encoding);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        KeyBoardEndpoint that = (KeyBoardEndpoint) o;
        return debugMode == that.debugMode &&
                Arrays.equals(alphabet, that.alphabet) &&
                Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(super.hashCode(), charset, debugMode);
        result = 31 * result + Arrays.hashCode(alphabet);
        return result;
    }
}
