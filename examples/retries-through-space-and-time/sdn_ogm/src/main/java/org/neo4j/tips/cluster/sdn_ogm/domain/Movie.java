package org.neo4j.tips.cluster.sdn_ogm.domain;

import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Movie")
public class Movie {

	@Id @GeneratedValue
	private Long id;

	private String title;

	Movie() {
	}

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
