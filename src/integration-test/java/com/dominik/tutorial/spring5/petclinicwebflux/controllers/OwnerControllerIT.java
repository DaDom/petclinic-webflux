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
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import com.dominik.tutorial.spring5.petclinicwebflux.testutils.FormDataMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("IT: Owner Controller")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class OwnerControllerIT {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 1;
    private static final int NUM_VISITS = 1;
    private static final int NUM_VETS = 1;

    private final OwnerService ownerService;
    private final PetService petService;
    private final VisitService visitService;
    private final WebTestClient webTestClient;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final VisitRepository visitRepository;
    private TestDataFactory testDataFactory;

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
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS, NUM_VETS);
    }

    @AfterEach
    void tearDown() {
        this.ownerRepository.deleteAll().block();
        this.petRepository.deleteAll().block();
        this.visitRepository.deleteAll().block();
    }

    @DisplayName("should update owner and show with pets")
    @Test
    void testEditOwnerWithPet() {
        // given
        Pet pet = this.testDataFactory.getPet();
        Visit visit = this.testDataFactory.getVisit();
        Owner owner = this.testDataFactory.getOwner();

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
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
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

    @DisplayName("should create owner")
    @Test
    void testCreateOwner() {
        // given
        Owner owner = this.testDataFactory.getOwner();

        // when
        this.webTestClient.post()
                .uri("/owners/new")
                .body(BodyInserters.fromFormData(FormDataMapper.ownerToFormDataMap(owner)))
                .exchange()
                .expectStatus().is3xxRedirection();

        // then
        Owner savedOwner = this.ownerService.findAll().blockFirst();
        assertNotNull(savedOwner);
        assertThat(owner).isEqualToIgnoringGivenFields(savedOwner, "id", "pets");
    }

    @DisplayName("should show owner details")
    @Test
    void testShowOwnerDetails() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        this.ownerService.save(owner).block();

        // when
        this.webTestClient.get()
                .uri("/owners/" + owner.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }
}
