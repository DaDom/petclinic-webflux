package com.dominik.tutorial.spring5.petclinicwebflux.controllers.webfluxtests;

import com.dominik.tutorial.spring5.petclinicwebflux.controllers.IndexController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("IT: Index Controller")
@WebFluxTest(controllers = IndexController.class)
class IndexControllerIT extends ControllerTestParent {

    private static final String EXPECTED_VIEW = "index";

    @Autowired
    private WebTestClient webTestClient;

    @DisplayName("should show index without trailing slash")
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
        this.verifyView(EXPECTED_VIEW, result);
    }

    @DisplayName("should show index with trailing slash")
    @Test
    void startPageWitSlash() throws Exception {
        String endpoint = "/";
        FluxExchangeResult result = this.webTestClient.get()
                .uri(endpoint)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(endpoint, result.getUriTemplate());
        this.verifyView(EXPECTED_VIEW, result);
    }
}