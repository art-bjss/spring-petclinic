package org.springframework.samples.petclinic.system;


import org.apache.commons.lang3.StringUtils;
import org.jfairy.Fairy;
import org.jfairy.producer.person.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class SampleData {

    private static final String DATE_FORMAT_STRING = "YYYY-MM-dd";
    private static final String VISIT_RABIES_SHOT = "rabies shot";
    private static final String VISIT_NEUTERED = "neutered";
    private static final String VISIT_SPAYED = "spayed";

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleData.class);

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private EntityManager entityManager;

    private List<PetType> petTypes = new ArrayList<>();

    @Transactional
    public void reset() {
        deleteAll();
        create();
    }

    @Transactional
    public void deleteAll() {
        Stream.of("Visit", "Pet", "Owner", "PetType", "Vet", "Specialty")
            .forEach(entityName -> entityManager.createQuery("DELETE from " + entityName).executeUpdate());
    }

    @Transactional
    public void create() {
        Specialty radiology = saveSpeciality("radiology");
        Specialty surgery = saveSpeciality("surgery");
        Specialty dentistry = saveSpeciality("dentistry");

        saveVet("James", "Carter");
        saveVet("Helen", "Leary", radiology);
        saveVet("Linda", "Douglas", surgery, dentistry);
        saveVet("Rafael", "Ortega", surgery);
        saveVet("Henry", "Stevens", radiology);
        saveVet("Sharon", "Jenkins");

        PetType cat = savePetType("cat");
        PetType dog = savePetType("dog");
        PetType lizard = savePetType("lizard");
        PetType snake = savePetType("snake");
        PetType bird = savePetType("bird");
        PetType hamster = savePetType("hamster");

        petTypes.addAll(Arrays.asList(cat, dog, lizard, snake, bird, hamster));

        Pet leo = createPet("Leo", "2010-09-07", cat);
        Pet basil = createPet("Basil", "2012-08-06", hamster);
        Pet rosy = createPet("Rosy", "2011-04-17", dog);
        Pet jewel = createPet("Jewel", "2010-03-07", dog);
        Pet iggy = createPet("Iggy", "2010-11-30", lizard);
        Pet george = createPet("George", "2010-01-20", snake);
        Pet samantha = createPet("Samantha", "2012-09-04", cat);
        Pet max = createPet("Max", "2012-09-04", cat);
        Pet lucky1 = createPet("Lucky", "2011-08-06", bird);
        Pet mulligan = createPet("Mulligan", "2007-02-24", dog);
        Pet freddy = createPet("Freddy", "2010-03-09", bird);
        Pet lucky2 = createPet("Lucky", "2010-06-24", dog);
        Pet sly = createPet("Sly", "2012-06-08", cat);

        saveOwner("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023", leo);
        saveOwner("Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749", basil);
        saveOwner("Eduardo", "Rodriquez", "2693 Commerce St.", "McFarland", "6085558763", rosy, jewel);
        saveOwner("Harold", "Davis", "563 Friendly St.", "Windsor", "6085553198", iggy);
        saveOwner("Peter", "McTavish", "2387 S. Fair Way", "Madison", "6085552765", george);
        saveOwner("Jean", "Coleman", "105 N. Lake St.", "Monona", "6085552654", samantha, max);
        saveOwner("Jeff", "Black", "1450 Oak Blvd.", "Monona", "6085555387", lucky1);
        saveOwner("Maria", "Escobito", "345 Maple St.", "Madison", "6085557683", mulligan);
        saveOwner("David", "Schroeder", "2749 Blackhawk Trail", "Madison", "6085559435", freddy);
        saveOwner("Carlos", "Estaban", "2335 Independence La.", "Waunakee", "6085555487", lucky2, sly);

        saveVisit(samantha, VISIT_RABIES_SHOT, "2013-01-01");
        saveVisit(max, VISIT_RABIES_SHOT, "2013-01-02");
        saveVisit(max, VISIT_NEUTERED, "2013-01-03");
        saveVisit(jewel, VISIT_SPAYED, "2013-01-04");

    }

    public void createLotsOfData() {
        Fairy fairy = Fairy.create();

        int count = 10000;
        IntStream.range(0, count).parallel()
            .forEach(countSoFar -> createRandomOwnerWithPets(fairy));

        LOGGER.info("Created {} owners", count);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRandomOwnerWithPets(Fairy fairy) {
        List<Pet> pets = IntStream.range(0, fairy.baseProducer().randomBetween(1, 5))
            .mapToObj(petNumber -> createPet(
                fairy.person().firstName(),
                fairy.dateProducer().randomDateInThePast(5).minusYears(3).toString(DATE_FORMAT_STRING),
                petTypes.get(fairy.baseProducer().randomBetween(0, petTypes.size() - 1)))
            ).collect(Collectors.toList());

        Person person = fairy.person();
        saveOwner(
            person.firstName(),
            person.lastName(),
            randomAddress(fairy),
            person.getAddress().getCity(),
            String.valueOf(fairy.baseProducer().randomBetween(100000000, 200000000)),
            pets.toArray(new Pet[]{})
        );

        pets.forEach(pet -> IntStream.range(1, fairy.baseProducer().randomBetween(1, 3)).forEach(unused2 -> saveVisit(
            pet,
            Arrays.asList(VISIT_NEUTERED, VISIT_RABIES_SHOT, VISIT_SPAYED).get(fairy.baseProducer().randomBetween(0, 2)),
            fairy.dateProducer().randomDateInThePast(3).toString(DATE_FORMAT_STRING)
        )));
    }


    private void saveVisit(Pet pet, String description, String visitDate) {
        Visit visit = new Visit();
        visit.setDescription(description);
        visit.setPetId(pet.getId());
        visit.setDate(fromString(visitDate));

        visitRepository.save(visit);
    }

    private Pet createPet(String name, String birthDate, PetType petType) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setBirthDate(fromString(birthDate));
        pet.setType(petType);

        return pet;
    }

    private void saveOwner(String firstName, String lastName, String address, String city, String telephone, Pet... pets) {
        Owner owner = new Owner();
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress(address);
        owner.setCity(city);
        owner.setTelephone(telephone);
        Stream.of(pets).forEach(owner::addPet);

        ownerRepository.save(owner);
    }

    private PetType savePetType(String typeName) {
        PetType petType = new PetType();
        petType.setName(typeName);

        entityManager.persist(petType);

        return petType;
    }

    private void saveVet(String firstName, String lastName, Specialty... specialties) {
        Vet vet = new Vet();
        vet.setFirstName(firstName);
        vet.setLastName(lastName);
        Stream.of(specialties).forEach(vet::addSpecialty);

        entityManager.persist(vet);
    }

    private Specialty saveSpeciality(String name) {
        Specialty radiology = new Specialty();
        radiology.setName(name);

        entityManager.persist(radiology);

        return radiology;
    }

    private Date fromString(String dateShortIso) {
        return Date.from(Instant.parse(dateShortIso + "T00:00:00Z"));
    }

    private String randomAddress(Fairy fairy) {
        return fairy.baseProducer().randomBetween(1, 100) + " " + StringUtils.capitalize(fairy.textProducer().word(1)) + " Street";
    }
}
