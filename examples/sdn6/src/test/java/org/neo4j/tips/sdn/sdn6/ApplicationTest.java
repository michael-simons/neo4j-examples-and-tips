package org.neo4j.tips.sdn.sdn6;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.tips.sdn.sdn6.movies.Actor;
import org.neo4j.tips.sdn.sdn6.movies.Movie;
import org.neo4j.tips.sdn.sdn6.movies.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
	"org.neo4j.migrations.locations-to-scan=classpath:neo4j/migrations,classpath:example-data" })
@Testcontainers(disabledWithoutDocker = true)
class ApplicationTest {
	@Container
	private static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:4.4")
		.withReuse(true);

	@DynamicPropertySource
	static void neo4jProperties(DynamicPropertyRegistry registry) {

		registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
		registry.add("spring.neo4j.authentication.username", () -> "neo4j");
		registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
	}

	@Test
	@DisplayName("GET /api/movies")
	void getMoviesShouldWork(@Autowired TestRestTemplate restTemplate) {

		var moviesResponse = restTemplate.exchange(
			"/api/movies",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<List<Movie>>() {
			});

		Consumer<Movie> theMovieCloudAtlas = m -> {
			assertThat(m.getActors())
				.map(Actor::getPerson)
				.extracting(Person::getName)
				.containsExactlyInAnyOrder("Hugo Weaving", "Jim Broadbent", "Tom Hanks", "Halle Berry");

			assertThat(m.getActors())
				.filteredOn(actor -> actor.getPerson().getName(), "Halle Berry")
				.singleElement()
				.extracting(Actor::getRoles).asList()
				.containsExactlyInAnyOrder("Luisa Rey", "Jocasta Ayrs", "Ovid", "Meronym");

			assertThat(m.getDirectors())
				.allSatisfy(p -> assertThat(p.getId()).isNotNull())
				.extracting(Person::getName)
				.containsExactlyInAnyOrder("Lana Wachowski", "Lilly Wachowski", "Tom Tykwer");
		};

		assertThat(moviesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(moviesResponse.getBody())
			.hasSize(38)
			.filteredOn(Movie::getTitle, "Cloud Atlas")
			.singleElement()
			.satisfies(theMovieCloudAtlas);
	}

	@Test
	@DisplayName("POST /api/people")
	void postPeopleShouldWork(@Autowired TestRestTemplate restTemplate, @Autowired Driver driver) {

		var peopleResponse = restTemplate.exchange(
			"/api/people",
			HttpMethod.POST,
			new HttpEntity<>(new Person("Lieschen Müller", 2020)),
			Person.class);

		assertThat(peopleResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(peopleResponse.getBody()).satisfies(person -> {
			assertThat(person.getId()).isNotNull();
			assertThat(person.getName()).isEqualTo("Lieschen Müller");
			assertThat(person.getBorn()).isEqualTo(2020);

			try (var session = driver.session()) {
				var cnt = session.run("MATCH (n:Person) WHERE n.id = $id RETURN count(n)",
					Map.of("id", person.getId().toString())).single().get(0).asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		});
	}
}
