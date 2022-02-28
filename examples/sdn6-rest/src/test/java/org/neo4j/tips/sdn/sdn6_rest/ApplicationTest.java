package org.neo4j.tips.sdn.sdn6_rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.tips.sdn.sdn6_rest.movies.Actor;
import org.neo4j.tips.sdn.sdn6_rest.movies.Movie;
import org.neo4j.tips.sdn.sdn6_rest.movies.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
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
	@DisplayName("GET /movies")
	void getMoviesShouldWork(@Autowired TestRestTemplate restTemplate) {
		var moviesResponse = restTemplate.exchange(
			"/movies?size={size}",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<PagedModel<Movie>>() {
			}, Map.of("size", "40"));

		assertThat(moviesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(moviesResponse.getBody().getContent())
			.hasSize(38)
			.filteredOn(Movie::getTitle, "Cloud Atlas")
			.hasSize(1);
	}

	@Test
	@DisplayName("POST /people")
	void postPeopleShouldWork(@Autowired TestRestTemplate restTemplate, @Autowired Driver driver) {

		var peopleResponse = restTemplate.exchange(
			"/people",
			HttpMethod.POST,
			new HttpEntity<>(new Person("Lieschen Müller", 2020)),
			Void.class);

		assertThat(peopleResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var location = peopleResponse.getHeaders().getLocation();
		var personResponse = restTemplate.getForEntity(location, Person.class);

		assertThat(personResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		var person = personResponse.getBody();
		assertThat(person.getName()).isEqualTo("Lieschen Müller");
		assertThat(person.getBorn()).isEqualTo(2020);

		try (var session = driver.session()) {
			var id = location.toString();
			id = id.substring(id.lastIndexOf("/") + 1);
			var cnt = session.run("MATCH (n:Person) WHERE n.id = $id RETURN count(n)",
				Map.of("id", id)).single().get(0).asLong();
			assertThat(cnt).isEqualTo(1L);
		}
	}
}
