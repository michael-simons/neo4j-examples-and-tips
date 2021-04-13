package org.neo4j.tips.sdn.sdn53withembeddedneo4joverbolt.domain;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Movie {
	@Id
	@GeneratedValue
	private Long id;

	private final String title;

	public Movie(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "Movie{" +
			"id=" + id +
			", title='" + title + '\'' +
			'}';
	}
}
