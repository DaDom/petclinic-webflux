package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document
public class Vet extends Person {
    private List<String> specialties;

    @Builder
    public Vet(UUID id, String firstName, String lastName, List<String> specialties) {
        super(id, firstName, lastName);
        this.specialties = specialties;
    }

    public Vet() {
        super();
    }
}
