package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CustomQueries {

	private final Neo4jClient neo4jClient;

	public CustomQueries(Neo4jClient neo4jClient) {
		this.neo4jClient = neo4jClient;
	}

	public <T> Collection<T> getAll(String query, Class<T> type, Map<String, Object> params,
		BiFunction<TypeSystem, Record, T> mapper) {
		return neo4jClient.query(query).bindAll(params).fetchAs(type)
			.mappedBy(mapper).all();
	}
}
