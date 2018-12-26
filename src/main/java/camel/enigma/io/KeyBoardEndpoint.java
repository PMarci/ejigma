package camel.enigma.io;

import camel.enigma.util.Properties;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;
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

    private static final String DEFAULT_ALPHABET_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final char[] DEFAULT_ALPHABET = DEFAULT_ALPHABET_STRING.toCharArray();

    private final char[] alphabet;
    private Charset charset;

    @UriParam
    private String encoding;
    @UriParam(label = "consumer", defaultValue = "false")
    private boolean debugMode = false;

    KeyBoardEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
        alphabet = DEFAULT_ALPHABET;
    }

    Exchange createExchange(Character input) {
        Exchange exchange = createExchange();
        ScrambleResult body = null;
        if (input == 18) {
            exchange.setProperty(Properties.RESET_OFFSETS, true);
        } else if (input == 2) {
            exchange.setProperty(Properties.DETAIL_MODE_TOGGLE, true);
        } else if (!Util.containsChar(alphabet, input)) {
            char upperCase = Character.toUpperCase(input);
            if (Util.containsChar(alphabet, upperCase)) {
                input = upperCase;
            }
        }
        if (Util.containsChar(alphabet, input)) {
            body = new ScrambleResult(input);
        }

        exchange.getIn().setBody(body);
        return exchange;
    }

    @Override
    public Producer createProducer() throws Exception {
        return null;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new KeyBoardConsumer(this, processor, debugMode);
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
