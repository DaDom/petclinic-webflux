package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = OwnerController.class)
class OwnerControllerTest {

    private static final String ENDPOINT_FIND_OWNER_FORM = "/owners/find";
    private static final String ENDPOINT_ADD_OWNER_FORM = "/owners/new";
    private static final String ENDPOINT_OWNER_DETAILS = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e";

    private static final String EXPECTED_HTML_FIND_OWNER = "<h2>Find Owners</h2>";
    private static final String EXPECTED_HMTL_ADD_OWNER = "<h2>Owner</h2>";
    private static final String EXPECTED_HTML_OWNER_DETAILS = "<h2>Owner Information</h2>";

    @MockBean
    private OwnerService ownerService;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testShowFindOwnerForm() {
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_FIND_OWNER_FORM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertTrue(new String(result.getResponseBodyContent()).contains(EXPECTED_HTML_FIND_OWNER));
    }

    @Test
    void testShowAddOwnerForm() {
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertTrue(new String(result.getResponseBodyContent()).contains(EXPECTED_HMTL_ADD_OWNER));
    }

    @Test
    void testCreateOwnerSuccess() {
        // given
        UUID id = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(id)
                .firstName("Dominik")
                .lastName("Picker")
                .address("Address")
                .city("City")
                .telephone("Phone")
                .build();
        when(this.ownerService.save(any())).thenReturn(Mono.just(owner));

        // when / then
        this.webTestClient.post()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/" + id.toString()));
    }

    @Test
    void testCreateOwnerMissingField() {
        Owner owner = Owner.builder()
                .lastName("Picker")
                .address("Address")
                .city("City")
                .telephone("Phone")
                .build();

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(ENDPOINT_ADD_OWNER_FORM, result.getUriTemplate());
    }

    @Test
    void testCreateOwnerBlankField() {
        Owner owner = Owner.builder()
                .lastName("   ")
                .address("Address")
                .city("City")
                .telephone("Phone")
                .build();

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(ENDPOINT_ADD_OWNER_FORM, result.getUriTemplate());
    }

    @Test
    void testOwnerDetailsFound() {
        // given
        UUID id = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(id)
                .build();
        when(this.ownerService.getById(any())).thenReturn(Mono.just(owner));

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_OWNER_DETAILS)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        verify(this.ownerService, times(1)).getById(any());
        assertTrue(new String(result.getResponseBodyContent()).contains(EXPECTED_HTML_OWNER_DETAILS));
    }

    @Test
    void testOwnerDetailsNotFound() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when / then
        this.webTestClient.get()
                .uri(ENDPOINT_OWNER_DETAILS)
                .exchange()
                .expectStatus().isNotFound();
        verify(this.ownerService, times(1)).getById(any());
    }

    private MultiValueMap<String, String> ownerToFormDataMap(Owner owner) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        if (owner.getFirstName() != null) {
            formData.add("firstName", owner.getFirstName());
        }
        if (owner.getLastName() != null) {
            formData.add("lastName", owner.getLastName());
        }
        if (owner.getAddress() != null) {
            formData.add("address", owner.getAddress());
        }
        if (owner.getCity() != null) {
            formData.add("city", owner.getCity());
        }
        if (owner.getTelephone() != null) {
            formData.add("telephone", owner.getTelephone());
        }

        return formData;
    }
}