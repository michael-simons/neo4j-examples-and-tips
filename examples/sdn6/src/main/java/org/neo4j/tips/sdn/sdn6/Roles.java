package org.neo4j.tips.sdn.sdn6;

import java.util.List;

import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
public class Roles {

	private final List<String> roles;

	public Roles(List<String> roles) {
		this.roles = roles;
	}

	public List<String> getRoles() {
		return roles;
	}
}
