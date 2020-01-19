package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceMongoTest {

    @Mock
    private PetService petService;
    @InjectMocks
    private VisitServiceMongo visitService;

    @Test
    void testCreateVisitOwnerNotExists() {
        // given
        when(this.petService.findById(any(), any())).thenReturn(
                Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, UUID.randomUUID().toString())));

        // when / then
        assertThrows(EntityNotFoundException.class,
                () -> this.visitService.createVisit(UUID.randomUUID(), UUID.randomUUID(), new Visit()).block());
    }

    @Test
    void testCreateVisitPetNotExists() {
        // given
        when(this.petService.findById(any(), any())).thenReturn(Mono.empty());

        // when / then
        assertThrows(EntityNotFoundException.class,
                () -> this.visitService.createVisit(UUID.randomUUID(), UUID.randomUUID(), new Visit()).block());
    }

    @Test
    void testCreateVisitValid() {
        // given
        UUID petId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();
        Pet pet = Pet.builder()
                .name("Some Pet")
                .birthDate(LocalDate.of(2011, 8, 23))
                .petType("Cat")
                .id(petId)
                .build();
        Visit visit = Visit.builder()
                .id(visitId)
                .description("OneVisit")
                .date(LocalDate.of(2015, 12, 13))
                .build();
        when(this.petService.findById(any(), any())).thenReturn(Mono.just(pet));
        when(this.petService.save(any(), any())).thenReturn(Mono.just(pet));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Pet.class);

        // when
        Visit result = this.visitService.createVisit(ownerId, petId, visit).block();

        // then
        verify(this.petService, times(1)).save(eq(ownerId), (Pet)captor.capture());
        Pet capturedPet = (Pet)captor.getValue();
        assertEquals(1, capturedPet.getVisits().size());
        assertThat(pet).isEqualToIgnoringGivenFields(capturedPet, "visits");
        assertThat(result).isEqualToComparingFieldByField(visit);
        assertThat(capturedPet.getVisits().get(0)).isEqualToComparingFieldByField(visit);
    }
}