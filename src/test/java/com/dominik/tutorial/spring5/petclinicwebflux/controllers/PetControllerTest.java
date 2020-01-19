package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.inmemory.PetTypeServiceInMemory;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PetController.class)
@ExtendWith(MockitoExtension.class)
@Import(PetTypeServiceInMemory.class)
class PetControllerTest extends ControllerTestParent {

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

    @Test
    void testShowNewPetFormOwnerExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));

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

    @Test
    void testCreatePetValidOwnerExists() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        Owner owner = Owner.builder().id(ownerId).build();
        Pet pet = Pet.builder()
                .id(petId)
                .name("Pet")
                .birthDate(LocalDate.of(2020,1,15))
                .petType("Cat")
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));
        when(this.petService.save(any(), any())).thenReturn(Mono.just(pet));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Pet.class);

        // when
        String url = "/owners/" + ownerId.toString() + "/pets/new";
        FluxExchangeResult result = this.webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(this.petToFormData(pet)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/" + ownerId.toString()))
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.petService, times(1)).save(eq(ownerId), (Pet)captor.capture());
        Pet capturedPet = (Pet)captor.getValue();
        assertThat(pet).isEqualToIgnoringGivenFields(capturedPet, "id");
    }

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

    @Test
    void testCreatePetInvalidOwnerExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        Pet pet = Pet.builder().petType("Whatever").build();

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_NEW_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(this.petToFormData(pet)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_NEW_PET_FORM, result);
    }

    @Test
    void testShowEditPetFormOwnerExistsPetExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(new Pet()));

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

    @Test
    void testShowEditPetFormOwnerExistsPetNotExists() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.empty());

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

    @Test
    void testEditPetOwnerExistsPetExists() {
        // given
        UUID petId = UUID.fromString("82ee7568-c925-43ae-ae96-a6d3f96e834e");
        String ownerId = "82ee7568-c925-43ae-ae96-a6d3f96e834e";
        Pet pet = Pet.builder()
                .name("Pet")
                .birthDate(LocalDate.of(2019, 11, 1))
                .petType("Cat")
                .id(petId)
                .build();
        Pet petFormData = Pet.builder()
                .id(petId)
                .name("NewPet")
                .birthDate(LocalDate.of(2011, 1, 1))
                .petType("Dog")
                .build();
        ArgumentCaptor captor = ArgumentCaptor.forClass(Pet.class);
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(pet));
        when(this.petService.save(any(), any())).thenReturn(Mono.just(pet));

        // when
        this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(this.petToFormData(petFormData)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("owners/" + ownerId));

        // then
        verify(this.petService, times(1)).save(any(), (Pet)captor.capture());
        Pet capturedPet = (Pet)captor.getValue();
        assertThat(petFormData).isEqualToIgnoringGivenFields(capturedPet, "id", "visits");
        assertEquals(capturedPet.getId().toString(), petId.toString());
    }

    @Test
    void testEditPetOwnerExistsPetExistsInvalid() {
        // given
        Pet pet = Pet.builder()
                .visits(new ArrayList<>())
                .birthDate(LocalDate.now())
                .name("MyCat")
                .id(UUID.randomUUID())
                .build();
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(this.petToFormData(pet)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_EDIT_PET_FORM, result);
    }

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

    @Test
    void testEditPetOwnerExistsPetNotExists() {
        // given
        Pet pet = Pet.builder()
                .visits(new ArrayList<>())
                .petType("Cat")
                .birthDate(LocalDate.now())
                .name("MyCat")
                .id(UUID.randomUUID())
                .build();
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));
        when(this.petService.findById(any(), any())).thenReturn(Mono.empty());

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(URL_EDIT_PET_FORM_VALID)
                .body(BodyInserters.fromFormData(this.petToFormData(pet)))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
    }

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

    private MultiValueMap<String, String> petToFormData(Pet pet) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (pet.getId() != null) {
            result.add("id", pet.getId().toString());
        }
        if (pet.getBirthDate() != null) {
            result.add("birthDate", pet.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (pet.getName() != null) {
            result.add("name", pet.getName());
        }
        if (pet.getPetType() != null) {
            result.add("petType", pet.getPetType().toString());
        }

        return result;
    }
}