package com.dominik.tutorial.spring5.petclinicwebflux.services;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OwnerService {

    Flux<Owner> findAll();
    Mono<Owner> getById(UUID id);
    Mono<Owner> save(Owner owner);
    Flux<Owner> findByLastNameFragment(String lastNameFragment);
}
