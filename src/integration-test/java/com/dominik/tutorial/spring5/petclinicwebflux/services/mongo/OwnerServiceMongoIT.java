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
public class OwnerServiceMongoIT {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final VisitRepository visitRepository;
    private OwnerService ownerService;
    private PetService petService;
    private VisitService visitService;

    @Autowired
    public OwnerServiceMongoIT(OwnerRepository ownerRepository, PetRepository petRepository, VisitRepository visitRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
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
    void testSave() {
        // given
        Owner owner = this.buildBasicOwner();
        Pet pet = this.buildBasicPet();

        // when
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), pet).block();

        // then
        Owner savedOwner = this.ownerService.getById(owner.getId()).block();
        List<Pet> savedOwnerPets = savedOwner.getPets();
        List<Pet> ownerPets = owner.getPets();
        assertThat(owner).isEqualToIgnoringGivenFields(savedOwner, "pets");
        assertEquals(1, savedOwnerPets.size());
        assertThat(pet).isEqualToIgnoringGivenFields(savedOwnerPets.get(0), "visits");
    }

    @Test
    void testSaveAndUpdate() {
        // given
        Owner owner = this.buildBasicOwner();
        Pet pet = this.buildBasicPet();

        // when
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), pet).block();

        Pet newPet = Pet.builder()
                .id(UUID.randomUUID())
                .name("Doreen")
                .birthDate(LocalDate.of(2011, 1, 2))
                .petType("Cat")
                .build();
        List<Pet> petList = owner.getPets();
        owner.setPets(petList);
        owner.setFirstName("Another firstname");
        owner.setLastName("Another lastname");
        owner.setAddress("Another address");
        owner.setCity("Another city");
        owner.setTelephone("Another phone");
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), newPet).block();

        // then
        Owner savedOwner = this.ownerService.getById(owner.getId()).block();
        List<Pet> savedOwnerPets = savedOwner.getPets();
        List<Pet> ownerPets = owner.getPets();
        assertThat(owner).isEqualToIgnoringGivenFields(savedOwner, "pets");
        assertEquals(2, savedOwnerPets.size());
        assertThat(pet).isEqualToIgnoringGivenFields(savedOwnerPets.get(0), "visits");
        assertThat(newPet).isEqualToIgnoringGivenFields(savedOwnerPets.get(1), "visits");
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
