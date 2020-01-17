package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class VetServiceMongo implements VetService {

    private final VetRepository vetRepository;

    public VetServiceMongo(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @Override
    public Flux<Vet> findAll() {
        return this.vetRepository.findAll();
    }

    @Override
    public Mono<Vet> save(Vet vet) {
        return this.vetRepository.save(vet);
    }
}
