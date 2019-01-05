package camel.enigma.route;

import camel.enigma.model.Armature;
import camel.enigma.util.ScrambleResult;
import camel.enigma.util.SettingManager;
import org.apache.camel.BeanInject;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class EnigmaRouteBuilder extends RouteBuilder {

    // use with run configuration containing -Dspring-boot.run.arguments=--input.debug=true
    @Value("${input.debug}")
    private String debugMode;

    @BeanInject
    private Armature armature;

    @Override
    public void configure() throws Exception {

//        from("stream:file?fileName=src/main/resources/sup&scanStream=true&scanStreamDelay=100&retry=true&fileWatcher=true")
//            .to("stream:out")
//        ;

        //@formatter:off

        // TODO maybe look into making this resolve using spring too
        from("keyboard?debugMode=" + /*"{{input.debug}}" */ debugMode).routeId("internalChain")
                .choice()
                    .when(PredicateBuilder.isNotNull(ExpressionBuilder.bodyExpression()))
                        .to("direct:scramblerChain")
                    .otherwise()
                        .bean(SettingManager.class)
                        .stop()
                .end()
                .choice()
                    .when(exchange -> SettingManager.isDetailMode())
                        .setBody(exchange -> exchange.getIn().getBody(ScrambleResult.class).printHistory())
                    .otherwise()
                        .setBody(exchange -> Collections.singletonList(
                            String.valueOf(
                                exchange.getIn().getBody(ScrambleResult.class).getResultAsChar())))
                .end()
                .to("keyboard").id("lightBoard")
        ;

        from("direct:scramblerChain").routeId("scramblerChain")
                .bean(armature).id("armature")
        ;
        //@formatter:on
    }
}
