package com.dominik.tutorial.spring5.petclinicwebflux.services;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VisitService {

    Mono<Visit> createVisit(UUID petId, Visit visit);
    Flux<Visit> findByPet(UUID petId);
}
