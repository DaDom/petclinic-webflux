package com.dominik.tutorial.spring5.petclinicwebflux.controllers.webfluxtests;

import com.dominik.tutorial.spring5.petclinicwebflux.controllers.PetController;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.inmemory.PetTypeServiceInMemory;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("IT: Pet Controller")
@WebFluxTest(controllers = PetController.class)
@ExtendWith(MockitoExtension.class)
@Import(PetTypeServiceInMemory.class)
class PetControllerIT extends ControllerTestParent {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 2;
    private static final int NUM_VISITS = 1;
    private static final int NUM_VETS = 1;

    private static final String URL_NEW_PET_FORM_VALID = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e/pets/new";
    private static final String URL_NEW_PET_FORM_INVALID = "/owners/123/pets/new";
    private static final String URL_EDIT_PET_FORM_VALID = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e/pets/82ee7568-c925-43ae-ae96-a6d3f96e834e/edit";
    private static final String URL_EDIT_PET_FORM_INVALID_OWNER = "/owners/123/pets/82ee7568-c925-43ae-ae96-a6d3f96e834e/edit";
    private static final String URL_EDIT_PET_FORM_INVALID_PET = "/owners/82ee7568-c925-43ae-ae96-a6d3f96e834e/pets/123/edit";

    private static final String EXPECTED_VIEW_NEW_PET_FORM = "pets/createOrUpdatePetForm";
    private static final String EXPECTED_VIEW_EDIT_PET_FORM = "pets/createOrUpdatePetForm";
    private static final String EXPECTED_VIEW_400_ERROR = "400error";

    @MockBean
    private PetService petService;
    @MockBean
    private OwnerService ownerService;
    @Autowired
    private WebTestClient webTestClient;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS, NUM_VETS);
    }

    @DisplayName("should show new pet form")
    @Test
    void testShowNewPetFormOwnerExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_NEW_PET_FORM_VALID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_NEW_PET_FORM, result);
    }

    @DisplayName("should return 404 when showing add pet form for non-existing owner")
    @Test
    void testShowNewPetFormOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_NEW_PET_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when showing add pet form for invalid owner uuid")
    @Test
    void testShowNewPetFormInvalidOwnerUUID() {
        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_NEW_PET_FORM_INVALID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should create pet for valid input")
    @Test
    void testCreatePetValidOwnerExists() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        UUID ownerId = owner.getId();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));
        when(this.petService.save(any(), any())).thenReturn(Mono.just(pet));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Pet.class);

        // when
        String url = "/owners/" + ownerId.toString() + "/pets/new";
        this.webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(FormDataMapper.petToFormData(pet)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/" + ownerId.toString()))
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.petService, times(1)).save(eq(ownerId), (Pet)captor.capture());
        Pet capturedPet = (Pet)captor.getValue();
        assertThat(pet).isEqualToIgnoringGivenFields(capturedPet, "id");
    }

    @DisplayName("should return 400 when creating pet for invalid owner UUID")
    @Test
    void testCreatePetInvalidOwnerUUID() {
        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_NEW_PET_FORM_INVALID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 404 when creating pet for non-existing owner")
    @Test
    void testCreatePetValidOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_NEW_PET_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should reject invalid input when creating pet")
    @Test
    void testCreatePetInvalidOwnerExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));
        Pet pet = this.testDataFactory.getPet();
        pet.setName(null);

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_NEW_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.petToFormData(pet)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_NEW_PET_FORM, result);
    }

    @DisplayName("should show edit pet form")
    @Test
    void testShowEditPetFormOwnerExistsPetExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(this.testDataFactory.getPet()));

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_EDIT_PET_FORM_VALID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_EDIT_PET_FORM, result);
    }

    @DisplayName("should return 404 when showing edit pet form for non-existing owner")
    @Test
    void testShowEditPetFormOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_EDIT_PET_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 404 when showing edit pet form for non-existing pet")
    @Test
    void testShowEditPetFormOwnerExistsPetNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_EDIT_PET_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when showing edit pet form for invalid owner uuid")
    @Test
    void testShowEditPetFormInvalidOwnerUUID() {
        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_EDIT_PET_FORM_INVALID_OWNER)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when showing edit pet form for invalid pet uuid")
    @Test
    void testShowEditPetFormInvalidPetUUID() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));

        // when
        FluxExchangeResult result = this.webTestClient.get()
                .uri(URL_EDIT_PET_FORM_INVALID_PET)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should edit pet for valid input")
    @Test
    void testEditPetOwnerExistsPetExists() {
        // given
        String ownerId = "82ee7568-c925-43ae-ae96-a6d3f96e834e";
        Pet pet = this.testDataFactory.getPets().get(0);
        Pet petFormData = this.testDataFactory.getPets().get(1);
        ArgumentCaptor captor = ArgumentCaptor.forClass(Pet.class);
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.just(pet));
        when(this.petService.save(any(), any())).thenReturn(Mono.just(pet));

        // when
        this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.petToFormData(petFormData)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("owners/" + ownerId));

        // then
        verify(this.petService, times(1)).save(any(), (Pet)captor.capture());
        Pet capturedPet = (Pet)captor.getValue();
        assertThat(petFormData).isEqualToIgnoringGivenFields(capturedPet, "id", "visits");
        assertEquals(capturedPet.getId().toString(), pet.getId().toString());
    }

    @DisplayName("should reject invalid input when editing pet")
    @Test
    void testEditPetOwnerExistsPetExistsInvalid() {
        // given
        Pet pet = this.testDataFactory.getPet();
        pet.setName("  ");
        when(this.ownerService.getById(any())).thenReturn(Mono.just(this.testDataFactory.getOwner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.petToFormData(pet)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_EDIT_PET_FORM, result);
    }

    @DisplayName("should return 404 when editing pet of non-existing owner")
    @Test
    void testEditPetOwnerNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 404 when editing non-existing pet")
    @Test
    void testEditPetOwnerExistsPetNotExists() {
        // given
        Pet pet = Pet.builder()
                .petType("Cat")
                .birthDate(LocalDate.now())
                .name("MyCat")
                .id(UUID.randomUUID())
                .build();
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findByIdAndOwner(any(), any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(FormDataMapper.petToFormData(pet)))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when editing pet for invalid owner uuid")
    @Test
    void testEditPetInvalidOwnerUUID() {
        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_INVALID_OWNER)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

    @DisplayName("should return 400 when editing pet for invalid pet uuid")
    @Test
    void testEditPetInvalidPetUUID() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_INVALID_PET)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }
}