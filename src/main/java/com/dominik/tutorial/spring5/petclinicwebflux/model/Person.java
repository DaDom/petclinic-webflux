package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class Person extends BaseEntity {
    private String firstName;
    private String lastName;

    public Person(UUID id, String firstName, String lastName) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Person() {
        super();
    }
}
