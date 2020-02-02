package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import com.dominik.tutorial.spring5.petclinicwebflux.testutils.FormDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("IT: Visit Controller")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class VisitControllerIT {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 1;
    private static final int NUM_VISITS = 1;
    private static final int NUM_VETS = 1;

    private final WebTestClient webTestClient;
    private final PetService petService;
    private final OwnerService ownerService;
    private TestDataFactory testDataFactory;

    @Autowired
    public VisitControllerIT(WebTestClient webTestClient, PetService petService, OwnerService ownerService) {
        this.webTestClient = webTestClient;
        this.petService = petService;
        this.ownerService = ownerService;
    }

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS, NUM_VETS);
    }

    @DisplayName("should create visit")
    @Test
    void testCreateVisit() {
        // given
        Pet pet = this.testDataFactory.getPet();
        Owner owner = this.testDataFactory.getOwner();
        Visit newVisit = this.testDataFactory.getVisit();
        UUID ownerId = owner.getId();
        UUID petId = pet.getId();

        // when
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), pet).block();
        this.webTestClient.post()
                .uri("/owners/" + ownerId.toString() + "/pets/" + petId.toString() + "/visits/new")
                .body(BodyInserters.fromFormData(FormDataMapper.visitToFormData(newVisit)))
                .exchange()
                .expectStatus().is3xxRedirection();
        Pet savedPet = this.petService.findByIdAndOwner(petId, ownerId).block();

        // then
        List<Visit> visitList = savedPet.getVisits();
        assertEquals(1, visitList.size());
        assertThat(newVisit).isEqualToIgnoringGivenFields(visitList.get(0), "id", "petId");
    }
}
