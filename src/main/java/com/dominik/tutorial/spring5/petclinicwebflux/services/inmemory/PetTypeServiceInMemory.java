package com.dominik.tutorial.spring5.petclinicwebflux.services.inmemory;

import com.dominik.tutorial.spring5.petclinicwebflux.services.PetTypeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PetTypeServiceInMemory implements PetTypeService {

    public static final String PET_TYPE_CAT = "Cat";
    public static final String PET_TYPE_DOG = "Dog";
    public static final String PET_TYPE_HORSE = "Horse";
    public static final String PET_TYPE_BIRD = "Bird";
    public static final String PET_TYPE_RABBIT = "Rabbit";

    @Override
    public Flux<String> findAll() {
        return Flux.just(
                PET_TYPE_CAT,
                PET_TYPE_DOG,
                PET_TYPE_BIRD,
                PET_TYPE_HORSE,
                PET_TYPE_RABBIT
        );
    }
}
