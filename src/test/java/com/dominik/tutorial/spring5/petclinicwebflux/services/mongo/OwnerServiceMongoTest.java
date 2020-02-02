package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@DisplayName("Owner Service Mongo")
@ExtendWith(MockitoExtension.class)
class OwnerServiceMongoTest {

    private static final int NUM_OWNERS = 2;
    private static final int NUM_PETS = 3;

    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private PetService petService;
    @InjectMocks
    private OwnerServiceMongo ownerServiceMongo;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS);
    }

    @DisplayName("should return all owners with pets on findAll")
    @Test
    void testFindAll() {
        // given
        given(this.ownerRepository.findAll()).willReturn(Flux.fromIterable(this.testDataFactory.getOwners()));
        given(this.petService.findByOwnerId(any(UUID.class))).willReturn(Flux.fromIterable(this.testDataFactory.getPets()));

        // when
        Flux<Owner> result = this.ownerServiceMongo.findAll();

        // then
        assertThat(result).isNotNull();
        List<Owner> resultList = result.collectList().block();
        assertThat(resultList).hasSize(NUM_OWNERS);
        for (int i = 0; i < NUM_OWNERS; i++) {
            assertThat(this.testDataFactory.getOwners().get(i)).isEqualToIgnoringGivenFields(resultList.get(i), "pets");
            assertThat(resultList.get(i).getPets()).hasSize(NUM_PETS);
        }
        then(this.ownerRepository).should(times(1)).findAll();
    }

    @DisplayName("should return nothing on findAll with empty DB")
    @Test
    void testFindAllEmpty() {
        // given
        given(this.ownerRepository.findAll()).willReturn(Flux.empty());

        // when
        Flux<Owner> result = this.ownerServiceMongo.findAll();

        // then
        assertThat(result).isNotNull();
        assertThat(result.hasElements().block()).isFalse();
    }

    @DisplayName("should return owner by ID with pets")
    @Test
    void testGetById() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        UUID idExists = owner.getId();
        UUID idNotExists = UUID.randomUUID();
        given(this.ownerRepository.findById(idExists)).willReturn(Mono.just(owner));
        given(this.ownerRepository.findById(idNotExists)).willReturn(Mono.empty());
        given(this.petService.findByOwnerId(idExists)).willReturn(Flux.fromIterable(this.testDataFactory.getPets()));

        // when
        Mono<Owner> resultMonoExists = this.ownerServiceMongo.getById(idExists);
        Mono<Owner> resultMonoNotExists = this.ownerServiceMongo.getById(idNotExists);

        // then
        Owner resultOwner = resultMonoExists.block();
        assertThat(resultMonoNotExists.hasElement().block()).isFalse();
        assertThat(resultOwner).isNotNull();
        assertThat(owner).isEqualToIgnoringGivenFields(resultOwner, "pets");
        assertThat(resultOwner.getPets()).hasSize(NUM_PETS);
        then(this.ownerRepository).should(times(1)).findById(idExists);
        then(this.ownerRepository).should(times(1)).findById(idNotExists);
        then(this.petService).should(times(1)).findByOwnerId(idExists);
        then(this.petService).shouldHaveNoMoreInteractions();
    }

    @DisplayName("should save new owner in repository")
    @Test
    void testSave() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        given(this.ownerRepository.save(any(Owner.class))).willReturn(Mono.just(owner));
        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);

        // when
        Owner result = this.ownerServiceMongo.save(owner).block();

        // then
        then(this.ownerRepository).should(times(1)).save(captor.capture());
        assertThat(result).isNotNull();
        assertThat(owner).isEqualToComparingFieldByField(result);
        assertThat(owner).isEqualToComparingFieldByField(captor.getValue());
    }

    @DisplayName("should find owner with pet by last name fragment")
    @Test
    void testFindByLastNameFragment() {
        // given
        String searchString = "anything";
        Owner owner = this.testDataFactory.getOwner();
        given(this.ownerRepository.findByLastNameContainingIgnoreCase(anyString())).willReturn(Flux.just(owner));
        given(this.petService.findByOwnerId(owner.getId())).willReturn(Flux.fromIterable(this.testDataFactory.getPets()));

        // when
        Flux<Owner> result = this.ownerServiceMongo.findByLastNameFragment(searchString);

        // then
        Owner resultOwner = result.blockFirst();
        assertThat(resultOwner).isNotNull();
        assertThat(owner).isEqualToIgnoringGivenFields(resultOwner, "pets");
        assertThat(resultOwner.getPets()).hasSize(NUM_PETS);
        then(this.ownerRepository).should(times(1)).findByLastNameContainingIgnoreCase(searchString);
    }
}