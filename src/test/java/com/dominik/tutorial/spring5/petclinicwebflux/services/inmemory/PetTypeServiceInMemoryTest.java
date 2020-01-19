package com.dominik.tutorial.spring5.petclinicwebflux.services.inmemory;

import com.dominik.tutorial.spring5.petclinicwebflux.services.PetTypeService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetTypeServiceInMemoryTest {

    @Test
    void testFindAll() {
        // given
        PetTypeService petTypeService = new PetTypeServiceInMemory();

        // when
        Flux<String> allTypes = petTypeService.findAll();
        List<String> allTypesList = allTypes.collectList().block();

        // then
        assertEquals(5, allTypesList.size());
        assertTrue(allTypesList.contains(PetTypeServiceInMemory.PET_TYPE_CAT));
        assertTrue(allTypesList.contains(PetTypeServiceInMemory.PET_TYPE_DOG));
        assertTrue(allTypesList.contains(PetTypeServiceInMemory.PET_TYPE_BIRD));
        assertTrue(allTypesList.contains(PetTypeServiceInMemory.PET_TYPE_HORSE));
        assertTrue(allTypesList.contains(PetTypeServiceInMemory.PET_TYPE_RABBIT));

    }
}