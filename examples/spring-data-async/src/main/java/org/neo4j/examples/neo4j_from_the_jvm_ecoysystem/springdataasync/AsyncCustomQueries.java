package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * THIS IS NOT MY PREFERRED SOLUTION!
 * Look at {@link OneClassSolution}!
 *
 * https://dzone.com/articles/spring-async-and-transaction
 * See https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-decl-explained
 * https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-understanding-aop-proxies
 * and https://docs.spring.io/spring-framework/docs/current/reference/html/images/tx.png
 * for reasons 2 classes are needed
 *
 * <strong>2 CLASSES ARE ONLY NEEDED WHEN CompletableFuture.supplyAsync IS USED! THAT WILL MESS UP THE ORDER OF PROXIES</strong>
 */
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
