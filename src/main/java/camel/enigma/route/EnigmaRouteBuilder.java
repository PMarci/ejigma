package camel.enigma.route;

import camel.enigma.model.Armature;
import camel.enigma.util.ScrambleResult;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EnigmaRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {

//        from("stream:file?fileName=src/main/resources/sup&scanStream=true&scanStreamDelay=100&retry=true&fileWatcher=true")
//            .to("stream:out")
//        ;

        from("keyboard?debugMode=true")
//                .setBody(simple("Key hit: ${body}"))
                .setBody(exchange -> new ScrambleResult(exchange.getIn().getBody(Character.class)))
                .bean(Armature.class)
                .setBody(exchange -> exchange.getIn().getBody(ScrambleResult.class).getResult())
                .to("stream:out")
        ;
    }
}
