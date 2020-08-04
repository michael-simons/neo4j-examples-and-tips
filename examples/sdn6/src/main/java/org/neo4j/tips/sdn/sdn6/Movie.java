package org.neo4j.tips.sdn.sdn6;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
public class Movie {

	@Id @GeneratedValue
	private Long id;

	private final String title;

	@Property("tagline")
	private final String description;

	@Relationship(type = "ACTED_IN", direction = INCOMING)
	private Map<Person, Roles> actorsAndRoles = new HashMap<>();

	@Relationship(type = "DIRECTED", direction = INCOMING)
	private List<Person> directors = new ArrayList<>();

	public Movie(String title, String description) { // <.>
		this.title = title;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Map<Person, Roles> getActorsAndRoles() {
		return actorsAndRoles;
	}

	public List<Person> getDirectors() {
		return directors;
	}
}
