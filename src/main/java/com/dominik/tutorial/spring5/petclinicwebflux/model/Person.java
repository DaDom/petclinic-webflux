package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public abstract class Person extends BaseEntity {

    @NotNull(message = "First name must be entered")
    @NotBlank(message = "First name may not be blank")
    @Length(min = 1, max = 200, message = "First name length must be between 1 and 200")
    private String firstName;

    @NotNull(message = "Last name must be entered")
    @NotBlank(message = "Last name may not be blank")
    @Length(min = 1, max = 200, message = "Last name length must be between 1 and 200")
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
