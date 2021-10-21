package camel.enigma.io;

import camel.enigma.model.Armature;
import camel.enigma.model.type.ConfigContainer;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class KeyBoardComponent extends DefaultComponent {

    @Autowired
    private Terminal terminal;

    @Autowired
    private Armature armature;

    @Autowired
    private ConfigContainer configContainer;

    public KeyBoardComponent() {
        this(null);
    }

    public KeyBoardComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new KeyBoardEndpoint(uri, this, terminal, configContainer, armature);
    }
}
