package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "spring.transaction.default-timeout = 2s")
class SpringDataAsyncApplicationTests {

	@Container
	private static final Neo4jContainer neo4j = new Neo4jContainer<>("neo4j:4.4")
		.withReuse(true)
		.withEnv("NEO4JLABS_PLUGINS", "[\"apoc\"]");

	@DynamicPropertySource
	static void neo4jProperties(DynamicPropertyRegistry registry) {

		registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
		registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
		registry.add("spring.neo4j.authentication.username", () -> "neo4j");
	}

	@Test
	void shouldExecuteSimpleFind(@Autowired MovieRepository movieRepository) {

		CompletableFuture<List<Movie>> movies = movieRepository.findAllByTitle("The Matrix");
		assertThat(movies)
			.succeedsWithin(Duration.ofSeconds(10))
			.satisfies(l -> Assertions.assertThat(l).hasSize(1));
	}

	@Test
	void shouldTimeOutWithCustomQuery(@Autowired MovieRepository movieRepository) {

		CompletableFuture<List<Movie>> movies = movieRepository.findAllAsync();
		assertThat(movies)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(InvalidDataAccessResourceUsageException.class)
			.withMessageStartingWith(
				"org.springframework.dao.InvalidDataAccessResourceUsageException: The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result.");
	}

	@Test
	void serviceDelegatingToRepoShouldFail(@Autowired MovieService movieService) {

		CompletableFuture<List<Movie>> movies = movieService.findAllAsync();
		assertThat(movies)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(InvalidDataAccessResourceUsageException.class)
			.withMessageStartingWith(
				"org.springframework.dao.InvalidDataAccessResourceUsageException: The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result.");
	}

	@Nested
	class NoDbInteraction {
		@Autowired MovieService movieService;

		@ParameterizedTest
		@ValueSource(ints = { 1, 3 })
		void noDbInteractionWillNeverTimeout(int seconds) {

			CompletableFuture<String> aFutureString = movieService.someRandomString(Duration.ofSeconds(seconds));
			assertThat(aFutureString)
				.succeedsWithin(Duration.ofSeconds(10))
				.isEqualTo("Foo");
		}
	}

	@Test
	void viaCustomExplicitTxCallInsideRepositoryFragment(@Autowired MovieRepository movieRepository) {

		// It does not take it's way through the repository and declarative mechanism, therefore the exceptions are a bit different
		CompletableFuture<String> aFutureString = movieRepository.getRandomString(Duration.ofSeconds(5));
		assertThat(aFutureString)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(IllegalStateException.class)
			.withMessageContaining("Transaction must be open, but has already been closed.");

		aFutureString = movieRepository.getRandomString(Duration.ofSeconds(1));
		assertThat(aFutureString)
			.succeedsWithin(Duration.ofSeconds(10))
			.isEqualTo("Foo");

		// Make sure they do run in parallel even with the completed futures (without using supplyAsync)
		int n = 10;
		CompletableFuture<?>[] futures = new CompletableFuture[n];

		for (int i = 0; i < n; ++i) {
			futures[i] = movieRepository.getRandomString(Duration.ofMillis(1500))
				.whenComplete((l, e) -> System.out.println(l));
		}

		assertThat(CompletableFuture.allOf(futures))
			.succeedsWithin(Duration.ofSeconds(n / 2));
	}

	@Test
	void customQueries2ClassesSolution(@Autowired AsyncCustomQueries asyncCustomQueries) {

		CompletableFuture<Collection<String>> futureStrings = asyncCustomQueries
			.getAll("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", Duration.ofSeconds(5).toMillis()), (t, r) -> r.get(0).asString());

		assertThat(futureStrings)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(IllegalStateException.class)
			.withMessageContaining("Transaction must be open, but has already been closed.");

		futureStrings = asyncCustomQueries
			.getAll("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", Duration.ofSeconds(1).toMillis()), (t, r) -> r.get(0).asString());

		assertThat(futureStrings)
			.succeedsWithin(Duration.ofSeconds(10))
			.asList().containsExactly("a", "b");
	}

	@Test
	void customQueries1ClassSolution(@Autowired OneClassSolution oneClassSolution) {

		CompletableFuture<Collection<String>> futureStrings = oneClassSolution
			.getAllAsync("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", Duration.ofSeconds(5).toMillis()), (t, r) -> r.get(0).asString());

		assertThat(futureStrings)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(IllegalStateException.class)
			.withMessageContaining("Transaction must be open, but has already been closed.");

		futureStrings = oneClassSolution
			.getAllAsync("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", Duration.ofSeconds(1).toMillis()), (t, r) -> r.get(0).asString());

		assertThat(futureStrings)
			.succeedsWithin(Duration.ofSeconds(10))
			.asList().containsExactly("a", "b");
	}

	@Test
	void repoDelegatingToCustomQueries(@Autowired MovieRepository movieRepository) {

		CompletableFuture<Collection<String>> futureStrings = movieRepository.getRandomStrings(Duration.ofSeconds(5));

		assertThat(futureStrings)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(IllegalStateException.class)
			.withMessageContaining("Transaction must be open, but has already been closed.");

		futureStrings = movieRepository.getRandomStrings(Duration.ofSeconds(1));

		assertThat(futureStrings)
			.succeedsWithin(Duration.ofSeconds(10))
			.asList().containsExactly("a", "b");
	}

	@Test
	void repoDelegatingToOneClassSolution(@Autowired MovieRepository movieRepository) {

		CompletableFuture<Collection<String>> futureStrings = movieRepository.getRandomStrings(Duration.ofSeconds(5));

		assertThat(futureStrings)
			.failsWithin(Duration.ofSeconds(10))
			.withThrowableOfType(ExecutionException.class)
			.withCauseInstanceOf(IllegalStateException.class)
			.withMessageContaining("Transaction must be open, but has already been closed.");

		futureStrings = movieRepository.getRandomStrings(Duration.ofSeconds(1));

		assertThat(futureStrings)
			.succeedsWithin(Duration.ofSeconds(10))
			.asList().containsExactly("a", "b");
	}

	@Test
	void makeSureEverythingRunsTogether(@Autowired MovieRepository movieRepository) {

		int n = 10;
		CompletableFuture<?>[] futures = new CompletableFuture[n];

		for (int i = 0; i < n; ++i) {
			futures[i] = movieRepository.getRandomStrings2(Duration.ofMillis(1500))
				.whenComplete((l, e) -> System.out.println(l));
		}

		assertThat(CompletableFuture.allOf(futures))
			.succeedsWithin(Duration.ofSeconds(n / 2));
	}

}
