package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document
public class Pet extends BaseEntity {

    @NotNull(message = "Name must be entered")
    @NotBlank(message = "Name may not be blank")
    @Length(min=1, max=200, message = "Name length must be between 1 and 200")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Birth date must be entered")
    @PastOrPresent(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotNull(message = "Pet type must be chosen")
    @NotBlank(message = "Pet type may not be blank")
    private String petType;

    @Transient
    private List<Visit> visits = new ArrayList<>();

    private UUID ownerId;

    @Builder
    public Pet(UUID id, String name, LocalDate birthDate, String petType, List<Visit> visits, UUID ownerId) {
        super(id);
        this.name = name;
        this.birthDate = birthDate;
        this.petType = petType;
        this.ownerId = ownerId;
        if (visits != null) {
            this.visits = visits;
        }
    }

    public Pet() {
        super();
    }
}
