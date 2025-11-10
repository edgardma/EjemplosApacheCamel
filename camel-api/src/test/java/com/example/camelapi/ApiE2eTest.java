package com.example.camelapi;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiE2eTest {

    static WireMockServer wireMock;

    @LocalServerPort int port;

    @Autowired TestRestTemplate rest;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        // Apunta la ruta al mock (reemplaza http://localhost:8083)
        r.add("destino.base-url", () -> wireMock.baseUrl());
    }

    @BeforeEach
    void stubDestino() {
        wireMock.resetAll();
        wireMock.stubFor(post(urlEqualTo("/api/destino"))
                .withRequestBody(matchingJsonPath("$[?(@.clienteId == 123)]"))
                .withRequestBody(matchingJsonPath("$[?(@.importe == 99.5)]"))
                .withRequestBody(matchingJsonPath("$[?(@.moneda == 'USD')]"))
                .willReturn(okJson("{\"status\":\"ok\",\"received\":true}")));
    }

    @Test
    void postOrigen_debeMapearYDelegar() {
        String body = "{\"user\":{\"id\":123},\"order\":{\"total\":99.5,\"currency\":\"USD\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = rest.postForEntity(
                "http://localhost:" + port + "/api/origen",
                new HttpEntity<>(body, headers),
                String.class
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("\"status\":\"ok\""));
        assertTrue(resp.getBody().contains("\"received\":true"));

        wireMock.verify(postRequestedFor(urlEqualTo("/api/destino")));
    }
}
