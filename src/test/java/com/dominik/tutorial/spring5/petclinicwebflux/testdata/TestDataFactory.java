package com.dominik.tutorial.spring5.petclinicwebflux.testdata;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestDataFactory {

    private static final int DEFAULT_RANDOM_STRING_LENGTH = 10;
    private static final int RANDOM_STRING_LIST_LENGTH_MIN = 2;
    private static final int RANDOM_STRING_LIST_LENGTH_MAX = 5;
    private static final int RANDOM_DATE_MIN_YEAR = 1950;
    private static final int RANDOM_DATE_MAX_YEAR = 2018;

    private List<Owner> owners;
    private List<Pet> pets;
    private List<Vet> vets;
    private List<Visit> visits;

    public static TestDataFactory petsOnly(int numPets) {
        return new TestDataFactory(0, numPets);
    }

    public static TestDataFactory vetsOnly(int numVets) {
        return new TestDataFactory(0, 0, 0, numVets);
    }

    public static TestDataFactory visitsOnly(int numVisits) {
        return new TestDataFactory(0, 0, numVisits, 0);
    }

    public TestDataFactory(int numOwners) {
        this(numOwners, 0);
    }

    public TestDataFactory(int numOwners, int numPets) {
        this(numOwners, numPets, 0);
    }

    public TestDataFactory(int numOwners, int numPets, int numVisits) {
        this(numOwners, numPets, numVisits, 0);
    }

    public TestDataFactory(int numOwners, int numPets, int numVisits, int numVets) {
        this.owners = new ArrayList<>();
        this.pets = new ArrayList<>();
        this.vets = new ArrayList<>();
        this.visits = new ArrayList<>();

        for (int i = 0; i < numOwners; i++) {
            this.owners.add(this.createOwner());
        }
        for (int i = 0; i < numPets; i++) {
            this.pets.add(this.createPet());
        }
        for (int i = 0; i < numVets; i++) {
            this.vets.add(this.createVet());
        }
        for (int i = 0; i < numVisits; i++) {
            this.visits.add(this.createVisit());
        }
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public List<Vet> getVets() {
        return vets;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public Owner getOwner() {
        return owners.get(0);
    }

    public Pet getPet() {
        return pets.get(0);
    }

    public Vet getVet() {
        return vets.get(0);
    }

    public Visit getVisit() {
        return visits.get(0);
    }

    private Owner createOwner() {
        return Owner.builder()
                .id(UUID.randomUUID())
                .firstName(this.randomString())
                .lastName(this.randomString())
                .address(this.randomString())
                .telephone(this.randomString())
                .city(this.randomString())
                .pets(new ArrayList<>())
                .build();
    }

    private Pet createPet() {
        return Pet.builder()
                .petType(this.randomString())
                .birthDate(this.randomDate())
                .name(this.randomString())
                .visits(new ArrayList<>())
                .id(UUID.randomUUID())
                .build();
    }

    private Vet createVet() {
        return Vet.builder()
                .id(UUID.randomUUID())
                .firstName(this.randomString())
                .lastName(this.randomString())
                .specialties(this.randomStringList())
                .build();
    }

    private Visit createVisit() {
        return Visit.builder()
                .id(UUID.randomUUID())
                .date(this.randomDate())
                .description(this.randomString())
                .build();
    }

    private LocalDate randomDate() {
        Random r = new Random();
        return LocalDate.of(
                r.nextInt(RANDOM_DATE_MAX_YEAR + 1) + RANDOM_DATE_MIN_YEAR,
                r.nextInt(12) + 1,
                r.nextInt(28) + 1
        );
    }

    private List<String> randomStringList() {
        int randomLength = new Random().nextInt(
                RANDOM_STRING_LIST_LENGTH_MAX - RANDOM_STRING_LIST_LENGTH_MIN + 1) + RANDOM_STRING_LIST_LENGTH_MIN;
        return this.randomStringList(randomLength);
    }

    private List<String> randomStringList(int desiredSize) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < desiredSize; i++) {
            result.add(this.randomString());
        }

        return result;
    }

    private String randomString() {
        return this.randomString(DEFAULT_RANDOM_STRING_LENGTH);
    }

    private String randomString(int length) {
        return RandomStringUtils.random(length, true, false);
    }
}
