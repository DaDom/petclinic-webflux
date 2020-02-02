package com.dominik.tutorial.spring5.petclinicwebflux.controllers.webfluxtests;

import com.dominik.tutorial.spring5.petclinicwebflux.controllers.OwnerController;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("IT: Owner Controller")
@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = OwnerController.class)
class OwnerControllerIT extends ControllerTestParent {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 2;
    private static final int NUM_VISITS = 1;
    private static final int NUM_VETS = 1;

    private static final String ENDPOINT_FIND_OWNER_FORM = "/owners/find";
    private static final String ENDPOINT_ADD_OWNER_FORM = "/owners/new";
    private static final String ENDPOINT_FIND_OWNERS = "/owners";
    private static final String ENDPOINT_OWNER_DETAILS_VALID = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e";
    private static final String ENDPOINT_OWNER_DETAILS_INVALID = "/owners/123";
    private static final String ENDPOINT_UPDATE_OWNER_VALID = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e/edit";
    private static final String ENDPOINT_UPDATE_OWNER_INVALID = "/owners/123/edit";

    private static final String EXPECTED_VIEW_FIND_OWNER = "owners/findOwners";
    private static final String EXPECTED_VIEW_ADD_OWNER = "owners/createOrUpdateOwnerForm";
    private static final String EXPECTED_VIEW_UPDATE_OWNER = "owners/createOrUpdateOwnerForm";
    private static final String EXPECTED_VIEW_OWNER_DETAILS = "owners/ownerDetails";
    private static final String EXPECTED_VIEW_OWNER_LIST = "owners/ownersList";
    private static final String EXPECTED_VIEW_400_ERROR = "400error";

    private static final String QUERY_PARAM_FIND_OWNERS = "lastName";

    @MockBean
    private OwnerService ownerService;
    @Autowired
    private WebTestClient webTestClient;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS, NUM_VETS);
    }

    @DisplayName("should show find owner form")
    @Test
    void testShowFindOwnerForm() {
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_FIND_OWNER_FORM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_FIND_OWNER, result);
    }

    @DisplayName("should show add owner form")
    @Test
    void testShowAddOwnerForm() {
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        this.verifyView(EXPECTED_VIEW_ADD_OWNER, result);
    }

    @DisplayName("should create owner with valid input")
    @Test
    void testCreateOwnerSuccess() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        when(this.ownerService.save(any())).thenReturn(Mono.just(owner));

        // when / then
        this.webTestClient.post()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/" + owner.getId().toString()));
    }

    @DisplayName("should not create owner with missing field")
    @Test
    void testCreateOwnerMissingField() {
        Owner owner = this.testDataFactory.getOwner();
        owner.setLastName(null);

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(ENDPOINT_ADD_OWNER_FORM, result.getUriTemplate());
        this.verifyView(EXPECTED_VIEW_ADD_OWNER, result);
    }

    @DisplayName("should not create owner with whitespace field")
    @Test
    void testCreateOwnerBlankField() {
        Owner owner = this.testDataFactory.getOwner();
        owner.setLastName("   ");

        // when / then
        FluxExchangeResult result = this.webTestClient.post()
                .uri(ENDPOINT_ADD_OWNER_FORM)
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        assertEquals(ENDPOINT_ADD_OWNER_FORM, result.getUriTemplate());
        this.verifyView(EXPECTED_VIEW_ADD_OWNER, result);
    }

    @DisplayName("should show owner details with pets and visits")
    @Test
    void testOwnerDetailsFound() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        owner.setPets(this.testDataFactory.getPets());
        for (Pet pet : owner.getPets()) {
            pet.setVisits(this.testDataFactory.getVisits());
        }
        when(this.ownerService.getById(any())).thenReturn(Mono.just(owner));

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_OWNER_DETAILS_VALID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);
        verify(this.ownerService, times(1)).getById(any());
        this.verifyView(EXPECTED_VIEW_OWNER_DETAILS, result);
    }

    @DisplayName("should return 404 when showing details for non-existing owner")
    @Test
    void testOwnerDetailsNotFound() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_OWNER_DETAILS_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .returnResult(FluxExchangeResult.class);
        verify(this.ownerService, times(1)).getById(any());
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should show 400 when showing details for invalid UUID")
    @Test
    void testOwnerDetailsInvalidUUID() {
        // when / then
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_OWNER_DETAILS_INVALID)
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(FluxExchangeResult.class);
        verifyNoInteractions(this.ownerService);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should redirect to search form when searching without query")
    @Test
    void testFindOwnersWithoutQuery() {
        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_FIND_OWNERS)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/find"))
                .returnResult(FluxExchangeResult.class);

        // then
        verifyNoInteractions(this.ownerService);
    }

    @DisplayName("should find all owners when searching with blank query")
    @Test
    void testFindOwnersWithBlankQuery() {
        // given
        when(this.ownerService.findAll()).thenReturn(Flux.empty());

        // when
        String url = UriComponentsBuilder.fromUriString(ENDPOINT_FIND_OWNERS)
                .queryParam(QUERY_PARAM_FIND_OWNERS, "")
                .toUriString();
        FluxExchangeResult result = this.webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.ownerService, times(1)).findAll();
        this.verifyView(EXPECTED_VIEW_OWNER_LIST, result);
    }

    @DisplayName("should search for owners when searching with non-blank query")
    @Test
    void testFindOwnersWithQuery() {
        // given
        when(this.ownerService.findByLastNameFragment(anyString())).thenReturn(Flux.empty());

        // when
        String url = UriComponentsBuilder.fromUriString(ENDPOINT_FIND_OWNERS)
                .queryParam(QUERY_PARAM_FIND_OWNERS, "anything")
                .toUriString();
        FluxExchangeResult result = this.webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.ownerService, times(1)).findByLastNameFragment(anyString());
        this.verifyView(EXPECTED_VIEW_OWNER_LIST, result);
    }

    @DisplayName("should show update owner form")
    @Test
    void testShowUpdateOwnerFormValid() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_UPDATE_OWNER_VALID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.ownerService, times(1)).getById(any());
        this.verifyView(EXPECTED_VIEW_UPDATE_OWNER, result);
    }

    @DisplayName("should return 400 when showing update owner form for invalid uuid")
    @Test
    void testShowUpdateOwnerFormInvalid() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_UPDATE_OWNER_INVALID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verifyNoInteractions(this.ownerService);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 404 when showing update owner form for non-existing owner")
    @Test
    void testShowUpdateOwnerFormNotFound() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(ENDPOINT_UPDATE_OWNER_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.ownerService, times(1)).getById(any());
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should update owner with valid input")
    @Test
    void testUpdateOwnerValid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        when(this.ownerService.save(any())).thenReturn(Mono.just(owner));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Owner.class);

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri("/owners/" + owner.getId().toString() + "/edit")
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/" + owner.getId().toString()))
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.ownerService, times(1)).save((Owner) captor.capture());
        Owner capturedOwner = (Owner)captor.getValue();
        assertEquals(owner.getId().toString(), capturedOwner.getId().toString());
    }

    @DisplayName("should reject input with missing field for update owner")
    @Test
    void testUpdateOwnerIncomplete() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        owner.setLastName(null);
        String id = owner.getId().toString();
        String url = "/owners/" + id + "/edit";

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verifyNoInteractions(this.ownerService);
        assertEquals(url, result.getUriTemplate());
        this.verifyView(EXPECTED_VIEW_UPDATE_OWNER, result);
    }

    @DisplayName("should return 400 when updating owner with invalid uuid")
    @Test
    void testUpdateOwnerInvalidUUID() {
        // given
        String id = "123";
        Owner owner = this.testDataFactory.getOwner();

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri("/owners/" + id + "/edit")
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verifyNoInteractions(this.ownerService);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }
}