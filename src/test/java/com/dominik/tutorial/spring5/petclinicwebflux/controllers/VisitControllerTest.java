package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = VisitController.class)
@ExtendWith(MockitoExtension.class)
class VisitControllerTest extends ControllerTestParent {

    private static final String URL_SHOW_CREATE_VISIT_FORM_VALID = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e/pets/82ee7568-c925-43ae-ae96-a6d3f96e834e/visits/new";
    private static final String URL_SHOW_CREATE_VISIT_FORM_INVALID_OWNER = "/owners/123/pets/82ee7568-c925-43ae-ae96-a6d3f96e834e/visits/new";
    private static final String URL_SHOW_CREATE_VISIT_FORM_INVALID_PET = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e/pets/123/visits/new";

    private static final String EXPECTED_VIEW_400_ERROR = "400error";
    private static final String EXPECTED_VIEW_SHOW_CREATE_VISIT_FORM = "pets/createOrUpdateVisitForm";

    @MockBean
    private OwnerService ownerService;
    @MockBean
    private PetService petService;
    @MockBean
    private VisitService visitService;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testShowCreateVisitFormValid() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(new Pet()));

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_SHOW_CREATE_VISIT_FORM, result);
    }

    @Test
    void testShowCreateVisitFormInvalidOwnerUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_INVALID_OWNER)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testShowCreateVisitFormInvalidPetUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_INVALID_PET)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testShowCreateVisitOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(new Pet()));

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testShowCreateVisitPetNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.empty());

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testCreateVisitValid() {
        // given
        UUID visitUUID = UUID.randomUUID();
        UUID ownerUUID = UUID.fromString("82ee7568-c925-43ae-ae96-a6d3f96e834e");
        Visit visit = Visit.builder()
                .id(visitUUID)
                .description("Description")
                .date(LocalDate.of(2015, 12, 1))
                .build();
        Pet pet = new Pet();
        when(this.ownerService.getById(any())).thenReturn(Mono.just(Owner.builder().id(ownerUUID).build()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(pet));
        when(this.visitService.createVisit(any(), any(), any())).thenReturn(Mono.just(visit));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Visit.class);

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(this.visitToFormData(visit)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("owners/" + ownerUUID.toString()))
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.visitService, times(1)).createVisit(any(), any(), (Visit)captor.capture());
        Visit capturedVisit = (Visit)captor.getValue();
        assertThat(visit).isEqualToIgnoringGivenFields(capturedVisit, "id");
    }

    @Test
    void testCreateVisitInvalid() {
        // given
        UUID visitUUID = UUID.randomUUID();
        UUID ownerUUID = UUID.fromString("82ee7568-c925-43ae-ae96-a6d3f96e834e");
        Visit visit = Visit.builder()
                .id(visitUUID)
                .date(LocalDate.of(2015, 12, 1))
                .build();
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(new Pet()));
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(this.visitToFormData(visit)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_SHOW_CREATE_VISIT_FORM, result);
    }

    @Test
    void testCreateVisitOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(new Pet()));
        Visit visit = Visit.builder()
                .id(UUID.randomUUID())
                .description("Description")
                .date(LocalDate.of(2015, 12, 1))
                .build();

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(this.visitToFormData(visit)))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testCreateVisitPetNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.empty());
        Visit visit = Visit.builder()
                .id(UUID.randomUUID())
                .description("Description")
                .date(LocalDate.of(2015, 12, 1))
                .build();

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(this.visitToFormData(visit)))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testCreateVisitInvalidOwnerUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_INVALID_OWNER)
                .body(BodyInserters.fromFormData(this.visitToFormData(Visit.builder().description("test").build())))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @Test
    void testCreateVisitInvalidPetUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_INVALID_PET)
                .body(BodyInserters.fromFormData(this.visitToFormData(Visit.builder().description("test").build())))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    private MultiValueMap<String, String> visitToFormData(Visit visit) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (visit.getId() != null) {
            result.add("id", visit.getId().toString());
        }
        if (visit.getDate() != null) {
            result.add("date", visit.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (visit.getDescription() != null) {
            result.add("description", visit.getDescription());
        }

        return result;
    }
}