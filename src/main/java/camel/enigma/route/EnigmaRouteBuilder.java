package camel.enigma.route;

import camel.enigma.model.Armature;
import camel.enigma.util.ScrambleResult;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.apache.camel.builder.PredicateBuilder.not;

@Component
public class EnigmaRouteBuilder extends RouteBuilder {

    // use with run configuration containing -Dspring-boot.run.arguments=--input.debug=true
    @Value("${input.debug}")
    private String debugMode;

    @Override
    public void configure() throws Exception {

//        from("stream:file?fileName=src/main/resources/sup&scanStream=true&scanStreamDelay=100&retry=true&fileWatcher=true")
//            .to("stream:out")
//        ;

        //@formatter:off

        // TODO maybe look into making this resolve using spring too
        from("keyboard?debugMode=" + /*"{{input.debug}}" */ debugMode)
                .setBody(exchange -> new ScrambleResult(exchange.getIn().getBody(Character.class)))
                .bean(Armature.class)
                .choice()
                    .when(exchangeProperty("detailMode"))
                        .setBody(exchange -> exchange.getIn().getBody(ScrambleResult.class).printHistory())
                    .endChoice()
                    .when(not(exchangeProperty("resetOffsets")))
                        .setBody(exchange -> exchange.getIn().getBody(ScrambleResult.class).getResultAsChar())
                    .otherwise()
                        .stop()
                .end()
                .to("stream:out?encoding=UTF-8")
        ;
        //@formatter:on
    }
}
