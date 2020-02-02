package com.dominik.tutorial.spring5.petclinicwebflux.controllers.webfluxtests;

import com.dominik.tutorial.spring5.petclinicwebflux.controllers.VetController;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@DisplayName("IT: Vet Controller")
@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = VetController.class)
class VetControllerIT extends ControllerTestParent {

    @MockBean
    private VetService vetService;
    @Autowired
    private WebTestClient webTestClient;

    @DisplayName("should list all vets")
    @Test
    void showAllVets() {
        // given
        when(this.vetService.findAll()).thenReturn(Flux.fromIterable(TestDataFactory.vetsOnly(3).getVets()));

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri("/vets.html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView("vets/index", result);
    }
}