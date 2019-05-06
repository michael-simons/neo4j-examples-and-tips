package org.neo4j.tips.ogm.choosing_identifier;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Neo4jContainer;

@RunWith(SpringRunner.class)
@DataNeo4jTest
public class RepositoriesTest {

	private static Neo4jContainer databaseServer = new Neo4jContainer();

	@BeforeClass
	public static void startContainer() {
		databaseServer.start();
	}

	@AfterClass
	public static void stopContainer() {
		databaseServer.stop();
	}

	@TestConfiguration
	static class Config {

		@Bean
		public org.neo4j.ogm.config.Configuration configuration() {
			return new Configuration.Builder()
				.uri(databaseServer.getBoltUrl())
				.credentials("neo4j", databaseServer.getAdminPassword())
				.build();
		}
	}

	@Autowired
	private PersonWithInternalSurrogateKeyRepository internalSurrogateKeyRepository;

	@Autowired
	private PersonWithExternalSurrogateKeyRepository externalSurrogateKeyRepository;

	@Autowired
	private TaxPayerRepository taxPayerRepository;

	@Autowired
	private BadPersonRepository badPersonRepository;

	@Test
	public void flowWithInternalSurrogateKeys() {
		PersonWithInternalSurrogateKey newPerson = new PersonWithInternalSurrogateKey();
		newPerson.setName("Homer");

		PersonWithInternalSurrogateKey savedPerson
			= internalSurrogateKeyRepository.save(newPerson);
		assertThat(savedPerson.getId()).isNotNull();

		Optional<PersonWithInternalSurrogateKey> loadedPerson
			= internalSurrogateKeyRepository.findById(savedPerson.getId());
		assertThat(loadedPerson).isPresent();
	}

	@Test
	public void flowWithExternalSurrogateKeys() {
		PersonWithExternalSurrogateKey newPerson = new PersonWithExternalSurrogateKey();
		newPerson.setName("Homer");

		PersonWithExternalSurrogateKey savedPerson
			= externalSurrogateKeyRepository.save(newPerson);
		assertThat(savedPerson.getId()).isNotNull();

		Optional<PersonWithExternalSurrogateKey> loadedPerson
			= externalSurrogateKeyRepository.findById(savedPerson.getId());
		Assertions.assertThat(loadedPerson).isPresent();
	}

	@Test
	public void flowWithBusinessKeys() {
		TaxPayer taxPayer = new TaxPayer("4711", "Michael");

		TaxPayer savedTaxPayer = taxPayerRepository.save(taxPayer);
		assertThat(savedTaxPayer).isEqualTo(taxPayer);

		Optional<TaxPayer> loadedPayer = taxPayerRepository.findById("4711");
		assertThat(loadedPayer).isPresent();
	}

	@Test
	public void flowWithBadPersons() {
		BadPerson person1 =  new BadPerson();
		person1.setId(42L);

		BadPerson savedPerson = badPersonRepository.save(person1);

		assertThat(badPersonRepository.findById(savedPerson.getInternalId())).isPresent();
		assertThat(badPersonRepository.findById(42L)).isEmpty();
	}
}
