package com.dominik.tutorial.spring5.petclinicwebflux.services;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VetService {

    Flux<Vet> findAll();
    Mono<Vet> save(Vet vet);
}
