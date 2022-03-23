package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Transactional
public interface MovieRepository extends Neo4jRepository<Movie, Long>, MovieRepositoryExt {

	@Async CompletableFuture<List<Movie>> findAllByTitle(String title);

	@Query("call apoc.util.sleep(4000) match (n:Movie) return n")
	@Async
	CompletableFuture<List<Movie>> findAllAsync();
}

interface MovieRepositoryExt {
	@Async
	CompletableFuture<String> getRandomString(Duration sleep);

	@Async
	CompletableFuture<Collection<String>> getRandomStrings(Duration sleep);

	@Async
	CompletableFuture<Collection<String>> getRandomStrings2(Duration sleep);
}

class MovieRepositoryImpl implements MovieRepositoryExt {

	private final Neo4jClient neo4jClient;

	private final TransactionTemplate transactionTemplate;

	private final AsyncCustomQueries asyncCustomQueries;

	private final OneClassSolution oneClassSolution;

	public MovieRepositoryImpl(Neo4jClient neo4jClient, TransactionTemplate transactionTemplate,
		AsyncCustomQueries asyncCustomQueries,
		OneClassSolution oneClassSolution
	) {
		this.neo4jClient = neo4jClient;
		this.transactionTemplate = transactionTemplate;
		this.asyncCustomQueries = asyncCustomQueries;
		this.oneClassSolution = oneClassSolution;
	}

	@Override
	public CompletableFuture<String> getRandomString(Duration sleep) {
		Supplier<String> stringSupplier = () -> transactionTemplate.execute(tx ->
			this.neo4jClient.query("call apoc.util.sleep($timeout) return 'Foo'")
				.bind(sleep.toMillis()).to("timeout")
				.fetchAs(String.class).one().get());

		// See that the tx template is applied *inside* the future, that order is paramount
		return CompletableFuture.supplyAsync(stringSupplier);
	}

	@Override public CompletableFuture<Collection<String>> getRandomStrings(Duration sleep) {
		return asyncCustomQueries
			.getAll("call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
				Map.of("timeout", sleep.toMillis()), (t, r) -> r.get(0).asString());
	}

	@Override public CompletableFuture<Collection<String>> getRandomStrings2(Duration sleep) {
		return oneClassSolution.getAllAsync(
			"call apoc.util.sleep($timeout) with ['a', 'b'] as x unwind x as y return y", String.class,
			Map.of("timeout", sleep.toMillis()), (t, r) -> r.get(0).asString());
	}
}