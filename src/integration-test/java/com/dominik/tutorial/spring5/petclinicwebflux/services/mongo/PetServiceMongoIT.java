package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("IT: Pet Service Mongo")
@DataMongoTest
public class PetServiceMongoIT {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 2;
    private static final int NUM_VISITS = 1;
    private static final int NUM_VETS = 1;

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;
    private final OwnerRepository ownerRepository;

    private PetService petService;
    private VisitService visitService;
    private OwnerService ownerService;

    private TestDataFactory testDataFactory;

    @Autowired
    public PetServiceMongoIT(PetRepository petRepository, VisitRepository visitRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
        this.ownerRepository = ownerRepository;
    }

    @BeforeEach
    void setUp() {
        this.visitService = new VisitServiceMongo(this.visitRepository);
        this.petService = new PetServiceMongo(this.petRepository, this.visitService);
        this.ownerService = new OwnerServiceMongo(this.ownerRepository, this.petService);

        this.visitRepository.deleteAll().block();
        this.petRepository.deleteAll().block();
        this.ownerRepository.deleteAll().block();

        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS, NUM_VETS);
    }

    @AfterEach
    void tearDown() {
        this.visitRepository.deleteAll().block();
        this.petRepository.deleteAll().block();
        this.ownerRepository.deleteAll().block();this.ownerRepository.deleteAll().block();
    }

    @DisplayName("should save a new pet")
    @Test
    void testAddPet() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet existingPet = this.testDataFactory.getPets().get(0);
        Pet newPet = this.testDataFactory.getPets().get(1);

        // when
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), existingPet).block();
        this.petService.save(owner.getId(), newPet).block();
        Owner savedOwner = this.ownerService.getById(owner.getId()).block();

        // then
        List<Pet> savedOwnerPetList = savedOwner.getPets();
        assertThat(owner).isEqualToIgnoringGivenFields(savedOwner, "pets");
        assertEquals(2, savedOwnerPetList.size());
        assertThat(existingPet).isEqualToIgnoringGivenFields(savedOwnerPetList.get(0), "visits");
        assertThat(newPet).isEqualToIgnoringGivenFields(savedOwnerPetList.get(1), "visits");
    }
}
