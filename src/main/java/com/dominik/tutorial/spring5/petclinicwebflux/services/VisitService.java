package com.dominik.tutorial.spring5.petclinicwebflux.services;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VisitService {

    Mono<Visit> createVisit(UUID ownerId, UUID petId, Visit visit);
}
