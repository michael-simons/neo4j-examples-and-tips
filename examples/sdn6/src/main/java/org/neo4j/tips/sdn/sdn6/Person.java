package org.neo4j.tips.sdn.sdn6;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public class Person {

	@Id @GeneratedValue
	private Long id;

	private final String name;

	private final Integer born;

	public Person(String name, Integer born) {
		this.name = name;
		this.born = born;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getBorn() {
		return born;
	}
}
