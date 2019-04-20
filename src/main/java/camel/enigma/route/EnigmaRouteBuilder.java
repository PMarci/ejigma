package camel.enigma.route;

import camel.enigma.model.Armature;
import org.springframework.stereotype.Component;

@Component
public class EnigmaRouteBuilder
//        extends RouteBuilder
{

//    @BeanInject
    private Armature armature;

//    @Override
    public void configure() throws Exception {

        //@formatter:off
//        from("keyboard").routeId("internalChain")
//                .choice()
//                    .when(PredicateBuilder.isNotNull(ExpressionBuilder.bodyExpression()))
//                        .to("direct:scramblerChain")
//                    .otherwise()
//                        .bean(SettingManager.class).id("settingManager")
//                        .stop()
//                .end()
//                .choice()
//                    .when(exchange -> !SettingManager.isDetailMode())
//                        .setBody(exchange -> Collections.singletonList(
//                            String.valueOf(
//                                exchange.getIn().getBody(ScrambleResult.class).getResultAsChar())))
//                .end()
//                .to("keyboard").id("lightBoard")
//        ;
//
//        from("direct:scramblerChain").routeId("scramblerChain")
//                .bean(armature).id("armature")
//        ;
        //@formatter:on
    }
}
