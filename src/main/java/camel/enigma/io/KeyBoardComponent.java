package camel.enigma.io;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import java.util.Map;

public class KeyBoardComponent extends DefaultComponent {

    public KeyBoardComponent() {
        this(null);
    }

    public KeyBoardComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new KeyBoardEndpoint(uri, this);
    }
}
