package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class Visit extends BaseEntity {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Date must be entered")
    @PastOrPresent(message = "Date must be in the past")
    private LocalDate date;

    @NotNull(message = "Description must be entered")
    @NotBlank(message = "Description may not be blank")
    @Length(min = 1, max = 500, message = "Description length must be between 1 and 500")
    private String description;

    @Builder
    public Visit(UUID id, LocalDate date, String description) {
        super(id);
        this.date = date;
        this.description = description;
    }

    public Visit() {
        super();
    }
}
