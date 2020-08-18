package org.neo4j.tips.cluster.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Movie {

	private final String title;

	@JsonCreator
	public Movie(@JsonProperty("title") String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	@Override public String toString() {
		return "Movie{" +
			"title='" + title + '\'' +
			'}';
	}
}
