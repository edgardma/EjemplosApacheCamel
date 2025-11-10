package com.example.camelapi;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamelSpringBootTest
@UseAdviceWith // no arranca rutas automáticamente
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = { "spring.main.web-application-type=none" } // sin Tomcat
)
class RouteMappingTest {

    @Autowired CamelContext camel;

    @EndpointInject("mock:dest")
    MockEndpoint dest;

    @Test
    void mappingDebeConstruirElJsonEsperado() throws Exception {
        // Sustituye cualquier endpoint http* por mock:dest ANTES de iniciar Camel
        AdviceWith.adviceWith(camel, "rest-source-to-rest-target", a ->
                a.weaveByToUri("http*").replace().to("mock:dest"));

        camel.start();

        dest.expectedMessageCount(1);

        camel.createProducerTemplate().sendBody(
                "direct:to-upstream",
                "{\"user\":{\"id\":123},\"order\":{\"total\":99.5,\"currency\":\"USD\"}}"
        );

        MockEndpoint.assertIsSatisfied(camel);
    }
}
