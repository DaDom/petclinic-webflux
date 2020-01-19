package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = OwnerController.class)
class OwnerControllerTest extends ControllerTestParent {

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
        this.verifyView(EXPECTED_VIEW_ADD_OWNER, result);
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
        this.verifyView(EXPECTED_VIEW_ADD_OWNER, result);
    }

    @Test
    void testOwnerDetailsFound() {
        // given
        UUID id = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(id)
                .firstName("Dominik")
                .lastName("Picker")
                .city("City")
                .address("Address")
                .telephone("Phone")
                .pets(List.of(
                        Pet.builder()
                                .name("Pet 1")
                                .petType("Cat")
                                .birthDate(LocalDate.now())
                                .visits(List.of(
                                        Visit.builder()
                                                .date(LocalDate.now())
                                                .description("Visit")
                                                .id(UUID.randomUUID())
                                                .build()
                                ))
                                .build(),
                        Pet.builder()
                                .name("Pet 2")
                                .petType("Dog")
                                .birthDate(LocalDate.now())
                                .visits(List.of(
                                        Visit.builder()
                                                .date(LocalDate.now())
                                                .description("Visit")
                                                .id(UUID.randomUUID())
                                                .build()
                                ))
                                .build()
                ))
                .build();
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

    @Test
    void testShowUpdateOwnerFormValid() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));

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

    @Test
    void testShowUpdateOwnerFormInvalid() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.just(new Owner()));

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

    @Test
    void testUpdateOwnerValid() {
        // given
        String id = UUID.randomUUID().toString();
        Owner owner = Owner.builder()
                .id(UUID.fromString(id))
                .firstName("Michael")
                .lastName("Jackson")
                .address("Neverland Ranch")
                .city("Las Vegas")
                .telephone("123123123")
                .build();
        when(this.ownerService.save(any())).thenReturn(Mono.just(owner));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Owner.class);

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri("/owners/" + id + "/edit")
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", endsWith("/owners/" + id))
                .returnResult(FluxExchangeResult.class);

        // then
        verify(this.ownerService, times(1)).save((Owner) captor.capture());
        Owner capturedOwner = (Owner)captor.getValue();
        assertEquals(id, capturedOwner.getId().toString());
    }

    @Test
    void testUpdateOwnerIncomplete() {
        // given
        String id = UUID.randomUUID().toString();
        String url = "/owners/" + id + "/edit";
        Owner owner = Owner.builder()
                .firstName("Michael")
                .address("Neverland Ranch")
                .city("Las Vegas")
                .telephone("123123123")
                .build();

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verifyNoInteractions(this.ownerService);
        assertEquals(url, result.getUriTemplate());
        this.verifyView(EXPECTED_VIEW_UPDATE_OWNER, result);
    }

    @Test
    void testUpdateOwnerInvalidUUID() {
        // given
        String id = "123";
        Owner owner = Owner.builder()
                .firstName("Michael")
                .lastName("Jackson")
                .address("Neverland Ranch")
                .city("Las Vegas")
                .telephone("123123123")
                .build();

        // when
        FluxExchangeResult result = this.webTestClient.post()
                .uri("/owners/" + id + "/edit")
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .returnResult(FluxExchangeResult.class);

        // then
        verifyNoInteractions(this.ownerService);
        this.verifyView(EXPECTED_VIEW_400_ERROR, result);
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