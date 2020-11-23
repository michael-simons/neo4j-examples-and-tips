package org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node
public final class Whatever {

	@Id @GeneratedValue
	private Long id;

	@Property("tagline")
	private final String description;

	public Whatever(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	@Override public String toString() {
		return "Whatever{" +
			   "id=" + id +
			   ", description='" + description + '\'' +
			   '}';
	}
}
