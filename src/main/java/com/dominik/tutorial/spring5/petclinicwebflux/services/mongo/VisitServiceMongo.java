package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VisitRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class VisitServiceMongo implements VisitService {

    private final VisitRepository visitRepository;

    public VisitServiceMongo(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public Mono<Visit> createVisit(UUID petId, Visit visit) {
        visit.setPetId(petId);
        return this.visitRepository.save(visit);
    }

    @Override
    public Flux<Visit> findByPet(UUID petId) {
        return this.visitRepository.findByPetId(petId);
    }
}
