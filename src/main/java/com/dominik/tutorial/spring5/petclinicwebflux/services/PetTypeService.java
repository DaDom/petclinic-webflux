package com.dominik.tutorial.spring5.petclinicwebflux.services;

import reactor.core.publisher.Flux;

public interface PetTypeService {

    Flux<String> findAll();
}
