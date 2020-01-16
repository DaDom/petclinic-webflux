package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Pet extends BaseEntity {
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private PetType petType;
    private List<Visit> visits = new ArrayList<>();

    @Builder
    public Pet(UUID id, String name, LocalDate birthDate, PetType petType, List<Visit> visits) {
        super(id);
        this.name = name;
        this.birthDate = birthDate;
        this.petType = petType;
        if (visits != null) {
            this.visits = visits;
        }
    }

    public Pet() {
        super();
    }
}
