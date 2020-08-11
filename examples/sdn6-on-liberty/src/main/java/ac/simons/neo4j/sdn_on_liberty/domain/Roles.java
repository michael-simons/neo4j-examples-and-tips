package ac.simons.neo4j.sdn_on_liberty.domain;

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
