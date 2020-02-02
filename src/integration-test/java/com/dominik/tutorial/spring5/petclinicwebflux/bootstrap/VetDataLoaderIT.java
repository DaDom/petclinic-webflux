package com.dominik.tutorial.spring5.petclinicwebflux.bootstrap;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.mongo.VetServiceMongo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("IT: Vet Data Loader")
@DataMongoTest
@Import({VetServiceMongo.class, VetDataLoader.class})
class VetDataLoaderIT {

    private final VetService vetService;
    private final VetDataLoader vetDataLoader;
    private final VetRepository vetRepository;

    @Autowired
    public VetDataLoaderIT(VetService vetService, VetDataLoader vetDataLoader, VetRepository vetRepository) {
        this.vetService = vetService;
        this.vetDataLoader = vetDataLoader;
        this.vetRepository = vetRepository;
    }

    @BeforeEach
    void setUp() {
        this.vetRepository.deleteAll().block();
    }

    @DisplayName("should create data when run on empty DB")
    @Test
    void testRunOnEmpty() throws Exception {
        // when
        this.vetDataLoader.run();

        // then
       assertEquals(2, this.vetService.findAll().count().block());
    }

    @DisplayName("should not create data when run on non-empty DB")
    @Test
    void testRunOnNotEmpty() throws Exception {
        // given
        this.vetService.save(new Vet()).block();

        // when
        this.vetDataLoader.run();

        // then
        assertEquals(1, this.vetService.findAll().count().block());
    }
}