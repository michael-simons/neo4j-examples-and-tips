package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // Must be applied to the whole service, otherwise the restrictions about proxies apply.
public class OneClassSolution {

	@Autowired
	private Neo4jClient neo4jClient;

	@Async
	public <T> CompletableFuture<Collection<T>> getAllAsync(String query, Class<T> type,
		Map<String, Object> params,
		BiFunction<TypeSystem, Record, T> mapper) {

		try {
			var result = getAll(query, type, params, mapper);
			return CompletableFuture.completedFuture(result);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public <T> Collection<T> getAll(String query, Class<T> type, Map<String, Object> params,
		BiFunction<TypeSystem, Record, T> mapper) {
		return neo4jClient.query(query).bindAll(params).fetchAs(type)
			.mappedBy(mapper).all();
	}
}