package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
public class PetServiceMongoIT {

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;
    private final OwnerRepository ownerRepository;

    private PetService petService;
    private VisitService visitService;
    private OwnerService ownerService;

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
    }

    @Test
    void testAddPet() {
        // given
        Owner owner = this.buildBasicOwner();
        Pet existingPet = this.buildBasicPet();
        Pet newPet = Pet.builder()
                .id(UUID.randomUUID())
                .name("Doreen")
                .petType("Cat")
                .birthDate(LocalDate.of(2011, 3, 14))
                .build();

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

    private Owner buildBasicOwner() {
        return Owner.builder()
                .id(UUID.randomUUID())
                .telephone("Phone")
                .address("Address")
                .city("City")
                .lastName("Lastname")
                .firstName("Firstname")
                .build();
    }

    private Pet buildBasicPet() {
        return Pet.builder()
                .id(UUID.randomUUID())
                .name("Rufus")
                .petType("Dog")
                .birthDate(LocalDate.of(2015, 12, 1))
                .build();
    }
}
