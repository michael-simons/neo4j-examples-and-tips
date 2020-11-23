package org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node
public final class Movie {

	@Id
	private final String title;

	@Property("tagline")
	private final String description;

	public Movie(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	@Override public String toString() {
		return "Movie{" +
			   "title='" + title + '\'' +
			   ", description='" + description + '\'' +
			   '}';
	}
}
