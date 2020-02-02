package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Pet Service Mongo")
@ExtendWith(MockitoExtension.class)
class PetServiceMongoTest {

    private static final int NUM_OWNERS = 0;
    private static final int NUM_PETS = 1;
    private static final int NUM_VISITS = 5;

    @Mock
    private VisitService visitService;
    @Mock
    private PetRepository petRepository;
    @InjectMocks
    private PetServiceMongo petService;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS);
    }

    @DisplayName("should find existing pet by pet ID")
    @Test
    void testFindByIdExistingPet() {
        // given
        Pet targetPet = this.testDataFactory.getPet();
        UUID petId = targetPet.getId();
        given(this.petRepository.findById(petId)).willReturn(Mono.just(targetPet));
        given(this.visitService.findByPet(petId)).willReturn(Flux.fromIterable(this.testDataFactory.getVisits()));

        // when
        Mono<Pet> result = this.petService.findById(petId);

        // then
        Pet resultPet = result.block();
        assertThat(resultPet).isNotNull();
        assertThat(targetPet).isEqualToIgnoringGivenFields(resultPet, "visits");
        assertThat(resultPet.getVisits()).hasSize(NUM_VISITS);
    }

    @DisplayName("should find existing pet by owner ID")
    @Test
    void testFindByOwnerIdExistingPet() {
        // given
        Pet targetPet = this.testDataFactory.getPet();
        UUID petId = targetPet.getId();
        UUID ownerId = UUID.randomUUID();
        given(this.petRepository.findByIdAndOwnerId(petId, ownerId)).willReturn(Mono.just(targetPet));
        given(this.visitService.findByPet(petId)).willReturn(Flux.fromIterable(this.testDataFactory.getVisits()));

        // when
        Mono<Pet> result = this.petService.findByIdAndOwner(petId, ownerId);

        // then
        Pet resultPet = result.block();
        assertThat(resultPet).isNotNull();
        assertThat(targetPet).isEqualToIgnoringGivenFields(resultPet, "visits");
        assertThat(resultPet.getVisits()).hasSize(NUM_VISITS);
    }

    @DisplayName("should not find any non-existing pet by owner ID")
    @Test
    void testFindByOwnerIdNotExistingPet() {
        // given
        UUID petId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        given(this.petRepository.findByIdAndOwnerId(petId, ownerId)).willReturn(Mono.empty());

        // when
        Mono<Pet> result = this.petService.findByIdAndOwner(petId, ownerId);

        // then
        assertThat(result.hasElement().block()).isFalse();
        then(this.petRepository).should(times(1)).findByIdAndOwnerId(petId, ownerId);
    }

    @DisplayName("should not find any non-existing pet by pet ID")
    @Test
    void testFindByIdNotExistingPet() {
        // given
        UUID petId = UUID.randomUUID();
        given(this.petRepository.findById(petId)).willReturn(Mono.empty());

        // when
        Mono<Pet> result = this.petService.findById(petId);

        // then
        assertThat(result.hasElement().block()).isFalse();
        then(this.petRepository).should(times(1)).findById(petId);
    }

    @DisplayName("should save new pet in repository")
    @Test
    void testSaveNewPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        Pet pet = this.testDataFactory.getPet();
        given(this.petRepository.save(eq(pet))).willReturn(Mono.just(pet));
        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);

        // when
        this.petService.save(ownerId, pet).block();

        // then
        then(this.petRepository).should(times(1)).save(captor.capture());
        Pet capturedPet = captor.getValue();
        assertThat(pet).isEqualToIgnoringGivenFields(capturedPet, "ownerId");
        assertThat(ownerId.toString()).isEqualTo(capturedPet.getOwnerId().toString());
    }

    @DisplayName("should delete a pet from repository")
    @Test
    void testDelete() {
        // given
        Pet pet = this.testDataFactory.getPet();
        given(this.petRepository.deleteById(pet.getId())).willReturn(Mono.empty());

        // when
        this.petService.delete(pet.getId()).block();

        // then
        then(this.petRepository).should(times(1)).deleteById(pet.getId());
    }
}