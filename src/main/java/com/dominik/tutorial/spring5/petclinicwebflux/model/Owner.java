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
public class Owner extends Person {
    private String address;
    private String city;
    private String telephone;
    private List<Pet> pets;

    @Builder
    public Owner(UUID id, String firstName, String lastName, String address, String city, String telephone, List<Pet> pets) {
        super(id, firstName, lastName);
        this.address = address;
        this.city = city;
        this.telephone = telephone;
        this.pets = pets;
    }

    public Owner() {
        super();
    }
}
