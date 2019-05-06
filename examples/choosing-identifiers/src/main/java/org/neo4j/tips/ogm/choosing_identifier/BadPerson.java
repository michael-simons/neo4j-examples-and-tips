package org.neo4j.tips.ogm.choosing_identifier;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

public class BadPerson {
	@Id @GeneratedValue
	private Long internalId;

	private Long id;

	public Long getInternalId() {
		return internalId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
