package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class PetServiceMongo implements PetService {

    private final PetRepository petRepository;
    private final VisitService visitService;

    public PetServiceMongo(PetRepository petRepository, VisitService visitService) {
        this.petRepository = petRepository;
        this.visitService = visitService;
    }

    @Override
    public Mono<Pet> findByIdAndOwner(UUID petId, UUID ownerId) {
        return this.petRepository.findByIdAndOwnerId(petId, ownerId)
                .flatMap(p -> this.addVisits(Mono.just(p), petId));
    }

    @Override
    public Mono<Pet> findById(UUID petId) {
        return this.petRepository.findById(petId)
                .flatMap(p -> this.addVisits(Mono.just(p), petId));
    }

    @Override
    public Flux<Pet> findByOwnerId(UUID ownerId) {
        return this.petRepository.findByOwnerId(ownerId)
                .flatMap(p -> this.addVisits(Mono.just(p), p.getId()));
    }

    @Override
    public Mono<Pet> save(UUID ownerId, Pet pet) {
        pet.setOwnerId(ownerId);
        return this.petRepository.save(pet);
    }

    @Override
    public Mono<Void> delete(UUID petId) {
        return this.petRepository.deleteById(petId);
    }

    /*
    TODO: Needs optimization
    This approach leads to a repository call for each single pet in the result, this is huge.
    Better approach would be to get all visits in the DB, potentially filtered by the given petUUID(s)
    and then add them programmatically to the right pet
     */
    private Mono<Pet> addVisits(Mono<Pet> petMono, UUID petId) {
        Mono<List<Visit>> visitListMono = this.visitService.findByPet(petId).collectList();
        return Mono.zip(petMono, visitListMono, (p, v) -> { p.setVisits(v); return p;});
    }
}
