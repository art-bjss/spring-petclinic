package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.system.SampleData;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided by the Spring
 * TestContext Framework: </p> <ul> <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li> <li><strong>Dependency Injection</strong> of test fixture instances, meaning that
 * we don't need to perform application context lookups. See the use of {@link Autowired @Autowired} on the <code>{@link
 * ClinicServiceTests#clinicService clinicService}</code> instance variable, which uses autowiring <em>by
 * type</em>. <li><strong>Transaction management</strong>, meaning each test method is executed in its own transaction,
 * which is automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script. <li> An {@link org.springframework.context.ApplicationContext
 * ApplicationContext} is also inherited and can be used for explicit bean lookup if necessary. </li> </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Dave Syer
 */

@RunWith(SpringRunner.class)
@DataJpaTest(includeFilters = { @ComponentScan.Filter(Service.class), @ComponentScan.Filter(Component.class) })
public class ClinicServiceTests {

    @Autowired
    protected OwnerRepository owners;

    @Autowired
    protected PetRepository pets;

    @Autowired
    protected VisitRepository visits;

    @Autowired
    protected VetRepository vets;

    @Autowired
    private SampleData sampleData;

    @Before
    public void resetData() {
        sampleData.reset();
    }

    @Test
    public void shouldFindOwnersByLastName() {
        Collection<Owner> owners = this.owners.findByLastName("Davis");
        assertThat(owners.size()).isEqualTo(2);

        owners = this.owners.findByLastName("Daviss");
        assertThat(owners.isEmpty()).isTrue();
    }

    @Test
    public void shouldFindSingleOwnerWithPet() {
        Collection<Owner> owners = this.owners.findByLastName("Franklin");
        assertThat(owners).hasSize(1);
        Owner owner = owners.iterator().next();
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets().size()).isEqualTo(1);
        assertThat(owner.getPets().get(0).getType()).isNotNull();
        assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
    }

    @Test
    public void shouldInsertOwner() {
        Collection<Owner> owners = this.owners.findByLastName("Schultz");
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.owners.save(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);

        owners = this.owners.findByLastName("Schultz");
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    public void shouldUpdateOwner() {
        Owner owner = this.owners.findByLastName("Franklin").iterator().next();
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        this.owners.save(owner);

        // retrieving new name from database
        owner = this.owners.findById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

    @Test
    public void shouldFindPetWithCorrectId() {
        Pet pet7 = this.pets.findById(7);
        assertThat(pet7.getId()).isEqualTo(7);
    }

    @Test
    public void shouldFindAllPetTypes() {
        Collection<PetType> petTypes = this.pets.findPetTypes();

        Set<String> names = petTypes.stream().map(PetType::getName).collect(Collectors.toSet());

        assertThat(names).contains("cat", "dog", "lizard", "snake", "bird", "hamster");

    }

    @Test
    public void shouldInsertPetIntoDatabaseAndGenerateId() {
        Owner owner6 = this.owners.findById(6);
        int found = owner6.getPets().size();

        Pet pet = new Pet();
        pet.setName("bowser");
        Collection<PetType> types = this.pets.findPetTypes();
        pet.setType(pets.findPetTypes().get(0));
        pet.setBirthDate(new Date());
        owner6.addPet(pet);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);

        this.pets.save(pet);
        this.owners.save(owner6);

        owner6 = this.owners.findById(6);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);
        // checks that id has been generated
        assertThat(pet.getId()).isNotNull();
    }

    @Test
    public void shouldUpdatePetName() throws Exception {
        Pet pet7 = this.pets.findById(7);
        String oldName = pet7.getName();

        String newName = oldName + "X";
        pet7.setName(newName);
        this.pets.save(pet7);

        pet7 = this.pets.findById(7);
        assertThat(pet7.getName()).isEqualTo(newName);
    }

    @Test
    public void shouldFindVets() {
        Collection<Vet> vets = this.vets.findAll();

        List<Vet> douglases = vets.stream().filter(vet -> vet.getLastName().equals("Douglas")).collect(Collectors.toList());
        assertThat(douglases).hasSize(1);
        Vet vet = douglases.get(0);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    public void shouldAddNewVisitForPet() {
        Pet pet = this.pets.findAll().stream()
            .filter(p -> !p.getVisits().isEmpty())
            .findFirst().get();
        int found = pet.getVisits().size();
        Visit visit = new Visit();
        pet.addVisit(visit);
        visit.setDescription("test");
        this.visits.save(visit);
        this.pets.save(pet);

        pet = this.pets.findById(pet.getId());
        assertThat(pet.getVisits().size()).isEqualTo(found + 1);
        assertThat(visit.getId()).isNotNull();
    }

    @Test
    public void shouldFindVisitsByPetId() throws Exception {
        Collection<Visit> visits = this.visits.findByPetId(7);
        assertThat(visits.size()).isEqualTo(2);
        Visit[] visitArr = visits.toArray(new Visit[visits.size()]);
        assertThat(visitArr[0].getDate()).isNotNull();
        assertThat(visitArr[0].getPetId()).isEqualTo(7);
    }

}
