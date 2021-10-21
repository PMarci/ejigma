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

    // TODO can this be improved? (hint: yes)
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
            int initialResult = alphabetString.indexOf(input);
            body = new ScrambleResult((initialResult != -1) ? initialResult : 0, alphabetString, input);
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
        keyBoard = new KeyBoard(this, processor, terminal);
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

    public void switchAlphabet(String newAlphabet) {
        setAlphabet(newAlphabet);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getAlphabetString() {
        return alphabetString;
    }

    public void setAlphabet(String alphabetString) {
        this.alphabetString = alphabetString;
        this.alphabet = alphabetString.toCharArray();
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

    public char[] getAlphabet() {
        return alphabet;
    }
}
