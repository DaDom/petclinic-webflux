package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class VisitServiceMongo implements VisitService {

    private final PetService petService;

    public VisitServiceMongo(PetService petService) {
        this.petService = petService;
    }

    @Override
    public Mono<Visit> createVisit(UUID ownerId, UUID petId, Visit visit) {
        return this.petService.findById(ownerId, petId)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Pet.class, petId.toString())))
                .flatMap(p -> {
                    p.getVisits().add(visit);
                    return Mono.just(p);
                })
                .flatMap(p -> this.petService.save(ownerId, p))
                .flatMap(p -> Mono.just(visit));
    }
}
