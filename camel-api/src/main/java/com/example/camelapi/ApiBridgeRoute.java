package com.example.camelapi;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiBridgeRoute extends RouteBuilder {
  @Override
  public void configure() {

    onException(HttpOperationFailedException.class)
      .handled(true)
      .maximumRedeliveries(1).redeliveryDelay(300)
      .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
      .setBody(simple("{\"error\":\"upstream_failed\",\"status\":${exception.statusCode}}"));

    restConfiguration()
      .component("servlet")
      .bindingMode(RestBindingMode.json)
      .contextPath("/api")
      .apiContextPath("/openapi")
      .apiProperty("api.title", "camel-api")
      .apiProperty("api.version", "1.0.0")
      .dataFormatProperty("prettyPrint", "true");

    rest()
      .post("/origen").consumes("application/json").produces("application/json")
      .to("direct:to-upstream");

    from("direct:to-upstream")
      .routeId("rest-source-to-rest-target")
      .log("Request in -> ${body}")
      .unmarshal().json()
      .setBody().jsonpath("{"
          + "\"clienteId\": $.user.id,"
          + "\"importe\": $.order.total,"
          + "\"moneda\": $.order.currency"
          + "}")
      .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
      .setHeader("Authorization", header("Authorization"))
      .toD("http://localhost:8083/api/destino?httpMethod=POST"
          + "&connectTimeout=3000&socketTimeout=5000")
      .unmarshal().json()
      .log("Response out <- ${body}");
  }
}
