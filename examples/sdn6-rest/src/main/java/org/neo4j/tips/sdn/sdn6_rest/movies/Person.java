package org.neo4j.tips.sdn.sdn6_rest.movies;

import java.util.UUID;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.fasterxml.jackson.annotation.JsonCreator;

@Node
public final class Person {

	@Id @GeneratedValue
	private final UUID id;

	private final String name;

	private Integer born;

	@PersistenceConstructor
	private Person(UUID id, String name, Integer born) {
		this.id = id;
		this.born = born;
		this.name = name;
	}

	@JsonCreator
	public Person(String name, Integer born) {
		this(null, name, born);
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getBorn() {
		return born;
	}

	public void setBorn(Integer born) {
		this.born = born;
	}
}
