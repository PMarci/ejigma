package camel.enigma.io;

import camel.enigma.model.Armature;
import camel.enigma.model.Scrambler;
import camel.enigma.model.type.ConfigContainer;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.Util;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

@UriEndpoint(firstVersion = "1.3.0", scheme = "keyboard", title = "KeyBoardEndpoint", syntax = "keyboard", consumerClass = KeyBoard.class, label = "system")
public class KeyBoardEndpoint extends DefaultEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(KeyBoardEndpoint.class);
    private final ConfigContainer configContainer;
    private final Armature armature;

    private String alphabetString;
    private char[] alphabet;
    private Charset charset;
    private final Terminal terminal;

    @UriParam
    private String encoding;
    @UriParam(label = "consumer", defaultValue = "false")
    private boolean debugMode = false;
    private KeyBoard keyBoard;
    private LightBoard lightBoard;

    KeyBoardEndpoint(String endpointUri,
                     Component component,
                     Terminal terminal,
                     ConfigContainer configContainer,
                     Armature armature) {
        super(endpointUri, component);
        this.configContainer = configContainer;
        this.armature = armature;
        alphabetString = Scrambler.DEFAULT_ALPHABET_STRING;
        alphabet = alphabetString.toCharArray();
        this.terminal = terminal;
    }

    Exchange createExchange(Character input) {
        Exchange exchange = createExchange();
        ScrambleResult body = null;
        if (!Util.containsChar(alphabetString, input)) {
            char upperCase = Character.toUpperCase(input);
            if (Util.containsChar(alphabetString, upperCase)) {
                input = upperCase;
            }
        }
        if (Util.containsChar(alphabetString, input)) {
            body = new ScrambleResult(alphabetString, input);
        }
        exchange.getIn().setBody(body);
        return exchange;
    }

    @Override
    public Producer createProducer() throws Exception {
        lightBoard = new LightBoard(this, terminal);
        return lightBoard;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        keyBoard = new KeyBoard(this, processor, debugMode, terminal);
        return keyBoard;
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

    public Terminal getTerminal() {
        return terminal;
    }

    public KeyBoard getKeyBoard() {
        return keyBoard;
    }

    public LightBoard getLightBoard() {
        return lightBoard;
    }

    public ConfigContainer getConfigContainer() {
        return configContainer;
    }

    public Armature getArmature() {
        return armature;
    }
}
