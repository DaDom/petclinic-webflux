package com.dominik.tutorial.spring5.petclinicwebflux.controllers.webfluxtests;

import com.dominik.tutorial.spring5.petclinicwebflux.controllers.VisitController;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import com.dominik.tutorial.spring5.petclinicwebflux.testutils.FormDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("IT: Visit Controller")
@WebFluxTest(controllers = VisitController.class)
@ExtendWith(MockitoExtension.class)
class VisitControllerIT extends ControllerTestParent {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 1;
    private static final int NUM_VISITS = 3;
    private static final int NUM_VETS = 1;

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
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS, NUM_VETS);
    }

    @DisplayName("should show create visit form")
    @Test
    void testShowCreateVisitFormValid() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(this.testDataFactory.getPet()));

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_SHOW_CREATE_VISIT_FORM, result);
    }

    @DisplayName("should return 400 when showing create visit form for invalid owner uuid")
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

    @DisplayName("should return 400 when showing create visit form for invalid pet uuid")
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

    @DisplayName("should return 404 when showing create visit form for non-existing owner")
    @Test
    void testShowCreateVisitOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(this.testDataFactory.getPet()));

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 404 when showing create visit form for non-existing pet")
    @Test
    void testShowCreateVisitPetNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.empty());

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should create visit with valid input")
    @Test
    void testCreateVisitValid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Visit visit = this.testDataFactory.getVisit();
        Pet pet = this.testDataFactory.getPet();
        when(this.ownerService.getById(any())).thenReturn(Mono.just(owner));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(pet));
        when(this.visitService.createVisit(any(), any())).thenReturn(Mono.just(visit));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Visit.class);
        String url = "/owners/" + owner.getId().toString() + "/pets/" + pet.getId().toString() + "/visits/new";

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(visit)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("owners/" + owner.getId().toString()))
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.visitService, times(1)).createVisit(any(), (Visit)captor.capture());
        Visit capturedVisit = (Visit)captor.getValue();
        assertThat(visit).isEqualToIgnoringGivenFields(capturedVisit, "id");
    }

    @DisplayName("should reject invalid input for creating visit")
    @Test
    void testCreateVisitInvalid() {
        // given
        Visit visit = this.testDataFactory.getVisit();
        visit.setDate(null);
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(this.testDataFactory.getPet()));
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(visit)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_SHOW_CREATE_VISIT_FORM, result);
    }

    @DisplayName("should return 404 when creating visit for non-existing owner")
    @Test
    void testCreateVisitOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(this.testDataFactory.getPet()));
        Visit visit = this.testDataFactory.getVisit();

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(visit)))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 404 when creating visit for non-existing pet")
    @Test
    void testCreateVisitPetNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(testDataFactory.getOwner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.empty());
        Visit visit = this.testDataFactory.getVisit();

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(visit)))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when creating visit for invalid owner uuid")
    @Test
    void testCreateVisitInvalidOwnerUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_INVALID_OWNER)
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(Visit.builder().description("test").build())))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when creating visit for invalid pet uuid")
    @Test
    void testCreateVisitInvalidPetUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_SHOW_CREATE_VISIT_FORM_INVALID_PET)
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(Visit.builder().description("test").build())))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }
}