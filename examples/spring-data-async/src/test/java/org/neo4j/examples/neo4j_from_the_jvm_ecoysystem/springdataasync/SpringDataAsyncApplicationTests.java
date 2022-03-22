package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// https://dzone.com/articles/spring-async-and-transaction
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

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private MovieService movieService;

	@Autowired
	private AsyncCustomQueries asyncCustomQueries;

	@Test
	void shouldExecuteSimpleFind() {

		CompletableFuture<List<Movie>> movies = movieRepository.findAllByTitle("The Matrix");
		await().until(movies::isDone);
		assertThat(movies).isCompletedWithValueMatching(l -> l.size() == 1);
	}

	@Disabled("Won't work with @EnableAsync")
	@Test
	void shouldTimeOutWithCustomQuery() {

		assertThatExceptionOfType(InvalidDataAccessResourceUsageException.class)
			.isThrownBy(movieRepository::findAllAsync)
			.withMessageStartingWith(
				"The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result. The transaction has not completed within the specified timeout");
	}

	@Disabled("Won't work with @EnableAsync")
	@Test
	void serviceDelegatingToRepoShouldFail() {

		assertThatExceptionOfType(InvalidDataAccessResourceUsageException.class)
			.isThrownBy(movieService::findAllAsync)
			.withMessageStartingWith(
				"The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result. The transaction has not completed within the specified timeout");
	}

	@Test
	void noDbInteractionShouldWorkInsideTimeout() {

		CompletableFuture<String> f = movieService.someRandomString(Duration.ofSeconds(1));
		await().until(f::isDone);
		assertThat(f).isCompletedWithValue("Foo");
	}

	@Test
	void noDbInteractionShouldWorkOutsideTimeout() {

		CompletableFuture<String> f = movieService.someRandomString(Duration.ofSeconds(3));
		await().until(f::isDone);
		assertThat(f).isCompletedWithValue("Foo");
	}

	@Test
	void dbInteractionShouldNotWorkOutsideTimeout() {

		CompletableFuture<String> f = movieRepository.getRandomString(Duration.ofSeconds(5))
			.handleAsync((s, e) -> {
				assertThat(e).hasCauseInstanceOf(ClientException.class).hasMessageStartingWith(
					"The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result. ");
				return null;
			});
		await().until(f::isDone);
		assertThat(f)
			.withFailMessage("Transaction must be open, but has already been closed.")
			.isCompletedExceptionally();
	}

	@Test
	void customQueriesInsideTimeout() {

		CompletableFuture<Collection<String>> strings = asyncCustomQueries
			.getAll("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", Duration.ofSeconds(1).toMillis()), (t, r) -> r.get(0).asString());

		await().until(strings::isDone);
		assertThat(strings).isCompletedWithValue(Arrays.asList("a", "b"));
	}

	@Test
	void customQueriesOutsideTimeout() {

		CompletableFuture<Collection<String>> strings = asyncCustomQueries
			.getAll("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", Duration.ofSeconds(4).toMillis()), (t, r) -> r.get(0).asString())
			.handleAsync((s, e) -> {
				assertThat(e).hasCauseInstanceOf(ClientException.class).hasMessageStartingWith(
					"The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result. ");
				return null;
			});
		await().until(strings::isDone);
		assertThat(strings)
			.withFailMessage("Transaction must be open, but has already been closed.")
			.isCompletedExceptionally();
	}
}
