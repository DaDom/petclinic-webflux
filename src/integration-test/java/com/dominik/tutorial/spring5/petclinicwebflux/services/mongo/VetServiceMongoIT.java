package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IT: Vet Service Mongo")
@DataMongoTest
class VetServiceMongoIT {

    private static final int NUM_VETS = 3;
    private final VetRepository vetRepository;
    private VetService vetService;
    private TestDataFactory testDataFactory;

    @Autowired
    public VetServiceMongoIT(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @BeforeEach
    void setUp() {
        this.testDataFactory = TestDataFactory.vetsOnly(NUM_VETS);
        this.vetService = new VetServiceMongo(this.vetRepository);
    }

    @AfterEach
    void tearDown() {
        this.vetRepository.deleteAll().block();
    }

    @DisplayName("should save vets and find all")
    @Test
    void testSaveAndList() {
        // given
        List<Vet> vets = this.testDataFactory.getVets();

        // when
        for (Vet vet : vets) {
            this.vetService.save(vet).block();
        }
        List<Vet> resultList = this.vetService.findAll().collectList().block();

        // then
        assertThat(resultList).hasSize(vets.size());
        for (int i = 0; i < resultList.size(); i++) {
            assertThat(vets.get(i)).isEqualToComparingFieldByField(resultList.get(i));
        }
    }
}