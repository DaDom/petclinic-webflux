package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@DisplayName("Visit Service Mongo")
@ExtendWith(MockitoExtension.class)
class VisitServiceMongoTest {

    private static final int NUM_VISIT = 5;

    @Mock
    private VisitRepository visitRepository;
    @InjectMocks
    private VisitServiceMongo visitService;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = TestDataFactory.visitsOnly(NUM_VISIT);
    }

    @DisplayName("should save new visit in repository")
    @Test
    void testCreateVisitValid() {
        // given
        UUID petId = UUID.randomUUID();
        Visit visit = this.testDataFactory.getVisit();
        given(this.visitRepository.save(any(Visit.class))).willReturn(Mono.just(visit));
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);

        // when
        Visit result = this.visitService.createVisit(petId, visit).block();

        // then
        then(this.visitRepository).should(times(1)).save(captor.capture());
        Visit capturedVisit = captor.getValue();
        assertThat(petId.toString()).isEqualTo(capturedVisit.getPetId().toString());
        assertThat(visit).isEqualToIgnoringGivenFields(capturedVisit, "petId");
    }

    @DisplayName("should find existing visits by pet ID")
    @Test
    void testFindByPet() {
        // given
        UUID petId = UUID.randomUUID();
        given(this.visitRepository.findByPetId(eq(petId))).willReturn(Flux.fromIterable(this.testDataFactory.getVisits()));

        // when
        List<Visit> result = this.visitService.findByPet(petId).collectList().block();

        // then
        assertThat(result).hasSize(NUM_VISIT);
        for (int i = 0; i < NUM_VISIT; i++) {
            assertThat(this.testDataFactory.getVisits().get(i)).isEqualToIgnoringGivenFields(result.get(i), "petId");
        }
    }
}