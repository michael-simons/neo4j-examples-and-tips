package org.neo4j.tips.ogm.choosing_identifier;

import java.util.UUID;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.id.UuidStrategy;

public class PersonWithExternalSurrogateKey {
	@Id @GeneratedValue(strategy = UuidStrategy.class)
	private UUID id;

	private String name;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
