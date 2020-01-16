package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class Visit extends BaseEntity {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String description;

    @Builder
    public Visit(UUID id, LocalDate date, String description) {
        super(id);
        this.date = date;
        this.description = description;
    }
}
