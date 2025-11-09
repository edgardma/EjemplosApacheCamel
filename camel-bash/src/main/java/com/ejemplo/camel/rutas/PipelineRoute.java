package com.ejemplo.camel.rutas;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PipelineRoute extends RouteBuilder {

    @Override
    public void configure() {

        // Manejo de errores: manda al log 'dead' con detalle y hace reintentos básicos
        errorHandler(deadLetterChannel("log:dead?level=ERROR&showAll=true&multiline=true")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000));

        // Pipeline: Timer -> HTTP GET (origen) -> Transform -> HTTP POST (destino)
        from("timer:pullSource?period={{app.poll.period}}")
                .routeId("rest-source-to-rest-target")

                // 1) Consumir servicio ORIGEN (GET)
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                // Usa URL completa desde config/env, p.ej. http://localhost:8082/api/usuarios/123
                .toD("{{app.source.url}}?httpMethod=GET")
                .convertBodyTo(String.class)
                .log("Origen (bruto): ${body}")

                // 2) Transformación robusta (sin templates Simple que puedan romper)
                .unmarshal().json()
                .process(exchange -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> in = exchange.getMessage().getBody(Map.class);

                    Map<String, Object> out = new HashMap<>();
                    out.put("idExterno", in.get("id"));
                    out.put("nombre", in.get("name"));
                    out.put("correo", in.get("email"));

                    Object status = in.get("status");
                    boolean activo = (status == null) || "active".equalsIgnoreCase(String.valueOf(status));
                    out.put("activo", activo);

                    exchange.getMessage().setBody(out);
                })
                .marshal().json()
                .log("Transformado: ${body}")

                // 3) Entregar al servicio DESTINO (POST)
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                // Si necesitas auth, descomenta y define TARGET_BEARER_TOKEN en env/k8s
                // .setHeader("Authorization", simple("Bearer {{TARGET_BEARER_TOKEN}}"))
                .toD("{{app.target.url}}")
                .log("Entregado OK al destino: ${body}");
    }
}
