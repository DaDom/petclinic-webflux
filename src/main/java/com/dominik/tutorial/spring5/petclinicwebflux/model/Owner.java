package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document
@Getter
@Setter
public class Owner extends Person {

    @NotNull(message = "Address must be entered")
    @NotBlank(message = "Address may not be blank")
    @Length(min = 1, max = 200, message = "Address length must be between 1 and 200")
    private String address;

    @NotNull(message = "City must be entered")
    @NotBlank(message = "City may not be blank")
    @Length(min = 1, max = 200, message = "City length must be between 1 and 200")
    private String city;

    @NotNull(message = "Telephone number must be entered")
    @NotBlank(message = "Telephone number may not be blank")
    @Length(min = 1, max = 200, message = "Telephone number length must be between 1 and 200")
    private String telephone;

    @Transient
    private List<Pet> pets = new ArrayList<>();

    @Builder
    public Owner(UUID id, String firstName, String lastName, String address, String city, String telephone, List<Pet> pets) {
        super(id, firstName, lastName);
        this.address = address;
        this.city = city;
        this.telephone = telephone;
        if (pets != null) {
            this.pets = pets;
        }
    }

    public Owner() {
        super();
    }
}
