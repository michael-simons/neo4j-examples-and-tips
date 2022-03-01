package org.neo4j.tips.sdn.sdn6_rest.movies;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@RelationshipProperties
public final class Actor {

	@Id @GeneratedValue @JsonIgnore
	private Long id;

	@TargetNode
	@JsonUnwrapped
	@JsonIgnoreProperties({ "id" })
	private final Person person;

	private final List<String> roles;

	public Actor(Person person, List<String> roles) {
		this.person = person;
		this.roles = new ArrayList<>(roles);
	}

	public Person getPerson() {
		return person;
	}

	public List<String> getRoles() {
		return List.copyOf(roles);
	}
}
