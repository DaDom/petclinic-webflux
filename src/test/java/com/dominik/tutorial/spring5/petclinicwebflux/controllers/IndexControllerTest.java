package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebFluxTest(controllers = IndexController.class)
class IndexControllerTest {

    private static final String EXPECTED_TEXT = "Welcome to Petclinic - Webflux version!";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void startPageWithoutSlash() throws Exception {
        String endpoint = "";
        FluxExchangeResult result = this.webTestClient.get()
                .uri(endpoint)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(endpoint, result.getUriTemplate());
        assertTrue(new String(result.getResponseBodyContent()).contains(EXPECTED_TEXT));
    }

    @Test
    void startPageWitSlash() throws Exception {
        // Testing endpoint "/"
        String endpoint = "/";
        FluxExchangeResult result = this.webTestClient.get()
                .uri(endpoint)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(endpoint, result.getUriTemplate());
        assertTrue(new String(result.getResponseBodyContent()).contains(EXPECTED_TEXT));
    }
}