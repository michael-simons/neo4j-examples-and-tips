package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.time.Duration;
import java.util.List;
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
	@Async CompletableFuture<List<Movie>> findAllAsync();
}

interface MovieRepositoryExt {
	@Async CompletableFuture<String> getRandomString(Duration sleep);
}

class MovieRepositoryImpl implements MovieRepositoryExt {

	private final Neo4jClient neo4jClient;

	private final TransactionTemplate transactionTemplate;

	public MovieRepositoryImpl(Neo4jClient neo4jClient, TransactionTemplate transactionTemplate) {
		this.neo4jClient = neo4jClient;
		this.transactionTemplate = transactionTemplate;
	}

	@Override public CompletableFuture<String> getRandomString(Duration sleep) {
		Supplier<String> stringSupplier = () -> transactionTemplate.execute(tx ->
			this.neo4jClient.query("call apoc.util.sleep($timeout) return 'Foo'")
				.bind(sleep.toMillis()).to("timeout")
				.fetchAs(String.class).one().get());

		// See that the tx template is applied *inside* the future, that order is paramount
		return CompletableFuture.supplyAsync(stringSupplier);
	}
}