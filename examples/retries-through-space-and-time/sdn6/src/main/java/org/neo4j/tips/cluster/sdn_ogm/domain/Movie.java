package org.neo4j.tips.cluster.sdn_ogm.domain;

import java.util.Objects;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node(primaryLabel = "Movie")
public class Movie {

	@Id @GeneratedValue
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Movie movie = (Movie) o;
		return title.equals(movie.title);
	}

	@Override public int hashCode() {
		return Objects.hash(title);
	}
}
