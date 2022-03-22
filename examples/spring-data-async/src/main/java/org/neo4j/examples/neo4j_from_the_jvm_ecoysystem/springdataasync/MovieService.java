package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class MovieService {

	private final Neo4jClient neo4jClient;

	private final Neo4jTemplate template;

	private final MovieRepository movieRepository;

	public MovieService(Neo4jClient neo4jClient, Neo4jTemplate template,
		MovieRepository movieRepository) {
		this.neo4jClient = neo4jClient;
		this.template = template;
		this.movieRepository = movieRepository;
	}

	@Async
	public CompletableFuture<List<Movie>> findAllAsync() {
		return movieRepository.findAllAsync();
	}

	// This method does never timeout, at least not because of Transactional, it just doesn't have a clue about
	// tx timeoutmanagement
	@Async
	public CompletableFuture<String> someRandomString(Duration sleep) {
		// The future is supplied async, in a different thread than the one the transaction is bound to!
		// this will never have the slightest idea it is running inside a transaction
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(sleep.toMillis());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return "Foo";
		});
	}
}
