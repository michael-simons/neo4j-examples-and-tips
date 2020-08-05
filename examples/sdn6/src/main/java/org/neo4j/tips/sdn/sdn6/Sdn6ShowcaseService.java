package org.neo4j.tips.sdn.sdn6;

import static org.neo4j.cypherdsl.core.Cypher.name;
import static org.neo4j.cypherdsl.core.Cypher.node;
import static org.neo4j.cypherdsl.core.Cypher.parameter;
import static org.neo4j.cypherdsl.core.Cypher.property;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.reactive.RxSession;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Sdn6ShowcaseService {

	private final Driver driver;

	private final ReactiveNeo4jClient client;

	private final ReactiveNeo4jTemplate template;

	private final MovieRepository movieRepository;

	public Sdn6ShowcaseService(Driver driver, ReactiveNeo4jClient client,
		ReactiveNeo4jTemplate template, MovieRepository movieRepository) {
		this.driver = driver;
		this.client = client;
		this.template = template;
		this.movieRepository = movieRepository;
	}

	public String callSimpleProcedureViaDriverImperative() {

		try (Session session = driver.session()) {
			return session.readTransaction(tx -> tx.run("call db.info() yield name").single().get("name").asString());
		}
	}

	public Flux<String> callSimpleProcedureViaDriverReactive() {
		return Flux.usingWhen(
			Mono.fromSupplier(driver::rxSession),
			session -> session.readTransaction(tx -> tx.run("call db.info() yield name").records()),
			RxSession::close
		).map(r -> r.get("name").asString());
	}

	// If you want to run that method in @Transactional, an additional
	// Transaction manager needs to be provided for the system (or any other database).
	public Flux<String> callSimpleProcedureViaClientInsideSpringTransaction() {
		return client.query("call db.info() yield name")
			.in("system") // Specifying the database is of course optional
			.fetchAs(String.class)
			.all();
	}

	@Transactional // That works only with the client, template and repositories
	public Flux<Person> findEntitiesViaTemplate(String nameRegex) {

		return this.template.findAll(
			Cypher // Showcase of the Cypher-DSL, but can also be a plain String based String
				.match(node("Person").named("n"))
				.where(
					property("n", "name").matches(parameter("name")) // Parameter names will be correctly escaped
				).returning(name("n"))
				.build(),
			Map.of("name", nameRegex),
			Person.class);
	}

	@Transactional
	public Mono<Person> saveEntityViaTemplate(Person person) {
		return this.template.save(person);
	}

	@Transactional
	public Flux<Movie> findEntitiesViaRepository(String titleRegex) {

		return this.movieRepository.findAllByTitleMatches(titleRegex);
	}

	@Transactional
	public Mono<Movie> saveEntityViaRepository(Movie movie) {
		return this.movieRepository.save(movie);
	}

	@Transactional
	public Flux<String> selectArbitraryThingsViaClient(Long movieId) {
		return this.client
			.query("MATCH (:Person) - [r:ACTED_IN] -> (m:Movie) WHERE id(m) = $id RETURN r.roles AS roles")
			// Another query would be, avoiding that embarrassing client side sorting and use ordering instead
			// .query("MATCH (:Person) - [r:ACTED_IN] -> (m:Movie) WHERE id(m) = $id WITH r UNWIND r.roles as role RETURN role ORDER BY role")
			.bind(movieId).to("id")
			.fetchAs(List.class)
			.mappedBy((t, r) -> r.get("roles").asList(v -> v.asString()))
			.all()
			.flatMap(list -> Flux.fromIterable((List<String>) list).sort(String::compareTo));
	}

	public Mono<Movie> findVirtualMovieViaApoc() {
		return movieRepository.findVirtualMovieViaApoc();
	}

	@Transactional
	public Flux<Movie> findByExample(Movie movie) {
		var byExample = Example.of(movie,
			ExampleMatcher.matchingAny()
				.withIgnoreNullValues()
				.withIgnoreCase().withStringMatcher(
			ExampleMatcher.StringMatcher.CONTAINING));
		return movieRepository.findAll(byExample);
	}
}
