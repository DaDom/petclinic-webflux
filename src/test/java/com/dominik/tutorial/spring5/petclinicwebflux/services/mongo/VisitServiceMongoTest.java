package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceMongoTest {

    @Mock
    private PetService petService;
    @Mock
    private VisitRepository visitRepository;
    @InjectMocks
    private VisitServiceMongo visitService;

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
        when(this.visitRepository.save(any())).thenReturn(Mono.just(visit));
        ArgumentCaptor captor = ArgumentCaptor.forClass(Visit.class);

        // when
        Visit result = this.visitService.createVisit(petId, visit).block();

        // then
        verify(this.visitRepository, times(1)).save((Visit)captor.capture());
        Visit capturedVisit = (Visit)captor.getValue();
        assertEquals(pet.getId(), capturedVisit.getPetId());
        assertThat(result).isEqualToIgnoringGivenFields(visit, "petId");
    }

    @Test
    void testFindByPet() {
        // given
        UUID petId = UUID.randomUUID();
        Visit visit = Visit.builder()
                .date(LocalDate.now())
                .description("Test Visit")
                .build();
        when(this.visitRepository.findByPetId(eq(petId))).thenReturn(Flux.just(visit));

        // when
        List<Visit> result = this.visitService.findByPet(petId).collectList().block();

        // then
        assertEquals(1, result.size());
        assertThat(visit).isEqualToComparingFieldByField(result.get(0));
    }
}