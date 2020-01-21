package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class OwnerControllerIT {

    private final OwnerService ownerService;
    private final PetService petService;
    private final VisitService visitService;
    private final WebTestClient webTestClient;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    @Autowired
    public OwnerControllerIT(OwnerService ownerService, PetService petService, VisitService visitService, WebTestClient webTestClient, OwnerRepository ownerRepository, PetRepository petRepository, VisitRepository visitRepository) {
        this.ownerService = ownerService;
        this.petService = petService;
        this.visitService = visitService;
        this.webTestClient = webTestClient;
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
    }

    @BeforeEach
    void setUp() {
        this.ownerRepository.deleteAll().block();
        this.petRepository.deleteAll().block();
        this.visitRepository.deleteAll().block();
    }

    @Test
    void testEditOwnerWithPet() {
        // given
        Pet pet = Pet.builder()
                .id(UUID.randomUUID())
                .name("Rufus")
                .birthDate(LocalDate.of(2012, 8, 21))
                .petType("Dog")
                .build();
        Visit visit = Visit.builder()
                .id(UUID.randomUUID())
                .description("Visit")
                .date(LocalDate.of(2015, 12, 1))
                .build();
        List<Pet> petList = new ArrayList<>();
        petList.add(pet);
        Owner owner = Owner.builder()
                .firstName("Firstname")
                .lastName("Lastname")
                .city("City")
                .address("Address")
                .telephone("Phone")
                .pets(petList)
                .build();

        // when
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), pet).block();
        this.visitService.createVisit(pet.getId(), visit).block();
        owner.setTelephone("New Phone");
        owner.setCity("New City");
        owner.setAddress("New Address");
        owner.setLastName("New Lastname");
        owner.setFirstName("New Firstname");
        this.webTestClient.post()
                .uri("/owners/" + owner.getId().toString() + "/edit")
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection();
        Owner savedOwner = this.ownerService.getById(owner.getId()).block();

        // then
        List<Pet> savedOwnerPetList = savedOwner.getPets();
        assertEquals(1, savedOwnerPetList.size());
        assertThat(pet).isEqualToIgnoringGivenFields(savedOwnerPetList.get(0), "visits", "ownerId");
        List<Visit> petVisitList = savedOwnerPetList.get(0).getVisits();
        assertEquals(1, petVisitList.size());
        assertThat(visit).isEqualToIgnoringGivenFields(petVisitList.get(0), "petId");
        assertThat(owner).isEqualToIgnoringGivenFields(savedOwner, "pets");
    }

    @Test
    void testCreateOwner() {
        // given
        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder()
                .lastName("Lastname")
                .firstName("Firstname")
                .city("City")
                .address("Address")
                .telephone("Phone")
                .id(ownerId)
                .build();

        // when
        this.webTestClient.post()
                .uri("/owners/new")
                .body(BodyInserters.fromFormData(this.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection();

        // then
        Owner savedOwner = this.ownerService.findAll().blockFirst();
        assertNotNull(savedOwner);
        assertThat(owner).isEqualToIgnoringGivenFields(savedOwner, "id", "pets");
    }

    @Test
    void testShowOwnerDetails() {
        // given
        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder()
                .lastName("Lastname")
                .firstName("Firstname")
                .city("City")
                .address("Address")
                .telephone("Phone")
                .id(ownerId)
                .build();
        this.ownerService.save(owner).block();

        // when
        this.webTestClient.get()
                .uri("/owners/" + owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
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
