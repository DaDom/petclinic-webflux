package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
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

@DisplayName("IT: Visit Service Mongo")
@DataMongoTest
class VisitServiceMongoIT {

    private final int NUM_OWNERS = 1;
    private final int NUM_PETS = 2;
    private final int NUM_VISITS = 3;

    private final VisitRepository visitRepository;
    private VisitService visitService;
    private TestDataFactory testDataFactory;

    @Autowired
    public VisitServiceMongoIT(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS);
        this.visitService = new VisitServiceMongo(this.visitRepository);
    }

    @AfterEach
    void tearDown() {
        this.visitRepository.deleteAll().block();
    }

    @DisplayName("should save visits and retrieve by pet")
    @Test
    void testSaveAndRetrieveByPet() {
        // given
        Pet pet1 = this.testDataFactory.getPets().get(0);
        Pet pet2 = this.testDataFactory.getPets().get(1);
        Visit visit1 = this.testDataFactory.getVisits().get(0);
        Visit visit2 = this.testDataFactory.getVisits().get(1);
        Visit visit3 = this.testDataFactory.getVisits().get(2);

        // when
        Visit resultVisit1 = this.visitService.createVisit(pet1.getId(), visit1).block();
        Visit resultVisit2 = this.visitService.createVisit(pet1.getId(), visit2).block();
        Visit resultVisit3 = this.visitService.createVisit(pet2.getId(), visit3).block();
        List<Visit> visitsOfPet1 = this.visitService.findByPet(pet1.getId()).collectList().block();
        List<Visit> visitsOfPet2 = this.visitService.findByPet(pet2.getId()).collectList().block();

        // then
        assertThat(visit1).isEqualToComparingFieldByField(resultVisit1);
        assertThat(visit2).isEqualToComparingFieldByField(resultVisit2);
        assertThat(visit3).isEqualToComparingFieldByField(resultVisit3);
        assertThat(visitsOfPet1).hasSize(2);
        assertThat(visitsOfPet2).hasSize(1);
        assertThat(visit1).isEqualToComparingFieldByField(visitsOfPet1.get(0));
        assertThat(visit2).isEqualToComparingFieldByField(visitsOfPet1.get(1));
        assertThat(visit3).isEqualToComparingFieldByField(visitsOfPet2.get(0));
    }
}