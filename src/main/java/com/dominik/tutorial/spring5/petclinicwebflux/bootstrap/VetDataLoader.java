package com.dominik.tutorial.spring5.petclinicwebflux.bootstrap;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VetDataLoader implements CommandLineRunner {

    private final VetService vetService;

    public VetDataLoader(VetService vetService) {
        this.vetService = vetService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (this.vetService.findAll().count().block() <= 0) {
            this.loadVets();
        }
    }

    private void loadVets() {
        Vet vet1 = Vet.builder()
                .firstName("Frank")
                .lastName("Schmitt")
                .specialties(List.of(
                        "Dental",
                        "Radiology"
                ))
                .build();
        this.vetService.save(vet1).block();

        Vet vet2 = Vet.builder()
                .firstName("Emma")
                .lastName("Watson")
                .specialties(List.of(
                        "Oncology",
                        "Physical Therapy"
                ))
                .build();
        this.vetService.save(vet2).block();

        System.out.println("Loaded vets");
    }
}
