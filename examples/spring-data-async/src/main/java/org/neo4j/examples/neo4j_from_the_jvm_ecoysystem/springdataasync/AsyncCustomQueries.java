package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncCustomQueries {

	private final CustomQueries customQueries;

	public AsyncCustomQueries(CustomQueries customQueries) {
		this.customQueries = customQueries;
	}

	@Async
	public <T> CompletableFuture<Collection<T>> getAll(String query, Class<T> type,
		Map<String, Object> params,
		BiFunction<TypeSystem, Record, T> mapper) {
		return CompletableFuture.supplyAsync(() -> customQueries.getAll(query, type, params, mapper));
	}
}
