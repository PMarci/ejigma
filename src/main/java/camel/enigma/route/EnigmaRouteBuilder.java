package camel.enigma.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EnigmaRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {

//        from("stream:file?fileName=src/main/resources/sup&scanStream=true&scanStreamDelay=100&retry=true&fileWatcher=true")
//            .to("stream:out")
//        ;

        from("keyboard")
                .setBody(simple("Key hit: ${body}"))
            .to("stream:out")
        ;
    }
}
